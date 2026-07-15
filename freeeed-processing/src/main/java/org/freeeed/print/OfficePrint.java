/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.print;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.freeeed.data.index.ComponentLifecycle;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.main.FreeEedMain;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Util;
import org.freeeed.util.LogFactory;
import org.freeeed.util.OsUtil;

import com.google.common.io.Files;

/**
 * Document conversions LibreOffice documentation pointers
 * https://ask.libreoffice.org/en/question/2641/convert-to-command-line-parameter/
 * 
 * For reference, commands like
 * soffice --convert-to html:"HTML" *.eml
 * should work, but they don't work well enough
 *
 * @author mark
 */
public class OfficePrint implements ComponentLifecycle {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(OfficePrint.class.getName());

    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(new String[]{"htm", "html", "txt", "csv", "odt", "ppt", "xls", "xlsx", "doc", "docx", "eml"});

    private static OfficePrint instance;

    private OfficePrint() {
    }

    public static synchronized OfficePrint getInstance() {
        if (instance == null) {
            instance = new OfficePrint();
        }
        return instance;
    }

    public void createPdf(File officeDocFile, String originalFileName) {
        String extension = Util.getExtension(officeDocFile.getPath());
        File outputFile = new File(officeDocFile.getPath() + ".pdf");
        if (extension == null || extension.isEmpty()) {
            extension = Util.getExtension(originalFileName);
        }

        try {
            if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
                // Strip external references up front so no renderer makes outbound
                // network calls (forensic soundness; also avoids hangs when soffice
                // imports saved web pages that reference remote/dead resources).
                File htmlToRender = sanitizeHtmlForOfflineRender(officeDocFile);
                if (htmlToRender == null) {
                    htmlToRender = officeDocFile;
                }
                try {
                    Html2Pdf.html2pdf(htmlToRender.getPath(), outputFile);
                } catch (Exception e) {
                    LOGGER.info("htmltopdf imaging not able to process file, trying OpenOffice imaging instead");
                    convertToPdfWithSOffice(htmlToRender, outputFile);
                }
                return;
            } else if ("pdf".equalsIgnoreCase(extension)) {
                Files.copy(officeDocFile, outputFile);
                return;
            } else if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(officeDocFile);
                convertToPDFUsingHtml(officeDocFile, outputFile, emlParser);
                return;
//            } else if ("nsfe".equalsIgnoreCase(extension)) {
//                NSFXDataParser emlParser = new NSFXDataParser(officeDocFile);
//                convertToPDFUsingHtml(officeDocFile, outputFile, emlParser);
//                return;
            } else {
                if (OsUtil.hasSOffice()) {
                    convertToPdfWithSOffice(officeDocFile, outputFile);
                } else {
                    // Fail loudly instead of silently producing nothing (issue #579).
                    LOGGER.warning("Imaging skipped for '" + officeDocFile.getName() + "' (type '"
                            + extension + "'): LibreOffice (soffice) is not available. Install or "
                            + "bundle LibreOffice to enable imaging of office documents.");
                }
                return;
            }
        } catch (Exception e) {
            LOGGER.severe("Problem creating PDF file for: " + e.getMessage());
        }

        try {
            IOUtils.copy(getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_PDF_IMAGE_FILE),
                    new FileOutputStream(outputFile));
        } catch (IOException e) {
            LOGGER.severe("Problem with default imaging: " + e.getMessage());
        }
    }
    public String emlToHtml(File file) {
        String html = "";
        try {
            EmlParser emlParser = new EmlParser(file);
            html = EmailUtil.createHtmlFromEmlFile(file.getPath(), emlParser);
        } catch (Exception e) {
            LOGGER.severe("Error converting to html: " + e.getMessage());
        }
        return html;
    }

    private void convertToPDFUsingHtml(File file, File outputPdf, EmailDataProvider emlParser) {
        try {
            String emlHtmlContent = EmailUtil.createHtmlFromEmlFile(file.getPath(), emlParser);
            Html2Pdf.htmlContent2Pdf(emlHtmlContent, outputPdf);
        } catch (Exception e) {
            LOGGER.severe("Cannot convert eml file: " + e.getMessage());
            convertToPdfWithSOffice(file, outputPdf);
        }
    }

    private static final String[] REMOTE_URL_ATTRS = {"src", "href", "background", "poster", "srcset", "data-src"};

    /**
     * Returns true if the URL points to a network location we must not fetch.
     */
    private static boolean isRemoteUrl(String url) {
        if (url == null) {
            return false;
        }
        String u = url.trim().toLowerCase();
        return u.startsWith("http://") || u.startsWith("https://")
                || u.startsWith("//") || u.startsWith("ftp://");
    }

    /**
     * Produce an offline-safe copy of an HTML file by removing every external
     * reference, so the PDF renderer never makes outbound network calls. This is
     * required for forensic soundness (no phoning home / tracking pixels) and it
     * also prevents soffice from hanging when a saved web page references remote
     * or dead resources.
     *
     * @param htmlFile the original HTML file
     * @return a sanitized HTML file alongside the original, or null on failure
     */
    private File sanitizeHtmlForOfflineRender(File htmlFile) {
        try {
            org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile, null);
            // Drop tags that only pull in remote content and add nothing to a static PDF.
            doc.select("script, link, base, iframe, frame, object, embed").remove();
            for (Element el : doc.getAllElements()) {
                for (String attr : REMOTE_URL_ATTRS) {
                    if (el.hasAttr(attr) && isRemoteUrl(el.attr(attr))) {
                        el.removeAttr(attr);
                    }
                }
                // Inline styles may pull in remote images via url(...).
                if (el.hasAttr("style") && el.attr("style").toLowerCase().contains("url(")) {
                    el.removeAttr("style");
                }
            }
            // Neutralize remote url(...) in embedded <style> blocks (CSS background
            // images), which the element loop above does not reach. Matches
            // url(http:// | https:// | ftp:// | //...) but leaves local/relative urls.
            String html = doc.outerHtml().replaceAll(
                    "(?i)url\\(\\s*['\"]?\\s*(?:https?:|ftp:)?//[^)]*\\)", "none");
            File sanitized = new File(htmlFile.getParentFile(),
                    FilenameUtils.removeExtension(htmlFile.getName()) + "_offline.html");
            FileUtils.writeStringToFile(sanitized, html, StandardCharsets.UTF_8);
            return sanitized;
        } catch (Exception e) {
            LOGGER.warning("Could not sanitize HTML for offline rendering: " + e.getMessage());
            return null;
        }
    }

    /**
     *
     * soffice commandline exampple soffice --headless --convert-to
     * pdf:writer_pdf_Export --outdir . AdminContracts.doc
     *
     * @param inputFile
     * @param outputFile
     * @return
     */
    public boolean convertToPdfWithSOffice(File inputFile, File outputFile) {
        String extension = Files.getFileExtension(inputFile.getAbsolutePath()).toLowerCase();

        // passing unsupported extension causes soffice to freeze, need to review which extensions are supported.
        if (SUPPORTED_EXTENSIONS.indexOf(extension) >= 0) {
            // Use a per-invocation user profile so a shared/locked default profile
            // (from a GUI instance, the startup system check, or a prior run) cannot
            // make headless --convert-to hang on the profile lock.
            String profileDir = new File(outputFile.getParentFile(), ".soffice_profile").getAbsolutePath();
            String fullCommand = OsUtil.getSOfficeExecutableLocation() + " --headless "
                    + " -env:UserInstallation=file://" + profileDir
                    + " --convert-to pdf:writer_pdf_Export " + inputFile.getAbsolutePath()
                    + " --outdir " + outputFile.getParentFile().getAbsolutePath();
            try {
                OsUtil.runCommand(fullCommand);
                File sofficeOutputFile = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + ".pdf");
                if (sofficeOutputFile.exists()) {
                    LOGGER.info("Created PDF at: " + sofficeOutputFile);
                    outputFile.delete();
                    FileUtils.moveFile(sofficeOutputFile, outputFile);
                    return true;
                } else {
                    LOGGER.warning("soffice did not produce PDF");
                    return false;
                }
            } catch (IOException e) {
                LOGGER.severe("Could not convert to PDF: " + e.getMessage());
            }
        }

        return false;
    }
}
