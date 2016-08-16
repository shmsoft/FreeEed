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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.io.IOUtils;
import org.freeeed.data.index.ComponentLifecycle;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Util;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Document conversions LibreOffice documentation pointers
 * https://ask.libreoffice.org/en/question/2641/convert-to-command-line-parameter/
 *
 * @author mark
 */
public class OfficePrint implements ComponentLifecycle {

    private static final Logger log = LoggerFactory.getLogger(OfficePrint.class);

	private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(new String[] { "htm", "html", "txt", "csv", "odt", "ppt", "xls", "xlsx", "doc", "docx", "eml" });

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
                try {
                    Html2Pdf.html2pdf(officeDocFile.getPath(), outputFile);
                } catch (Exception e) {
                    log.info("htmltopdf imaging not able to process file, trying OpenOffice imaging instead", e.getMessage());
                    convertToPdfWithSOffice(officeDocFile, outputFile);
                }
                return;
            } else if ("pdf".equalsIgnoreCase(extension)) {
                Files.copy(officeDocFile, outputFile);
                return;
            } else if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(officeDocFile);
                convertToPDFUsingHtml(officeDocFile, outputFile, emlParser);
                return;
            } else if ("nsfe".equalsIgnoreCase(extension)) {
                NSFXDataParser emlParser = new NSFXDataParser(officeDocFile);
                convertToPDFUsingHtml(officeDocFile, outputFile, emlParser);
                return;
            } else {
            	if (OsUtil.hasSOffice()) {
            		convertToPdfWithSOffice(officeDocFile, outputFile);
            	}
                return;
            }
        } catch (Exception e) {
            log.error("Problem creating PDF file for: {}", officeDocFile, e);
        }

        try {
            IOUtils.copy(getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_PDF_IMAGE_FILE),
                    new FileOutputStream(outputFile));
        } catch (IOException e) {
            log.error("Problem with default imaging", e);
        }
    }

    private void convertToPDFUsingHtml(File file, File outputPdf, EmailDataProvider emlParser) {
        try {
            String emlHtmlContent = EmailUtil.createHtmlFromEmlFile(file.getPath(), emlParser);
            Html2Pdf.htmlContent2Pdf(emlHtmlContent, outputPdf);
        } catch (Exception e) {
            log.error("Cannot convert eml file: {}", e.getMessage());
            convertToPdfWithSOffice(file, outputPdf);
        }
    }

    /**
     *
     * @param officeDocFile
     * @param output
     *
     * soffice commandline exampple soffice --headless --convert-to
     * pdf:writer_pdf_Export --outdir . AdminContracts.doc
     */
    public boolean convertToPdfWithSOffice(File inputFile, File outputFile) {
    	String extension = Files.getFileExtension(inputFile.getAbsolutePath()).toLowerCase();
    	
    	// passing unsupported extension causes soffice to freeze, need to review which extensions are supported.
    	if (SUPPORTED_EXTENSIONS.indexOf(extension) >= 0) {
    		String fullCommand = OsUtil.getSOfficeExecutableLocation() + " --headless "
    				+ " --convert-to pdf:writer_pdf_Export " + inputFile.getAbsolutePath()
    				+ " --outdir " + outputFile.getParentFile().getAbsolutePath();
    	   try {
    			OsUtil.runCommand(fullCommand);
    			File sofficeOutputFile = new File(FilenameUtils.removeExtension(inputFile.getAbsolutePath()) + ".pdf");
    			if (sofficeOutputFile.exists()) {
    				log.info("Created PDF at: " + sofficeOutputFile);
    				FileUtils.moveFile(sofficeOutputFile, outputFile);
    				return true;
    			} else {
    				log.warn("soffice did not produce PDF");
    				return false;
    			}
    		} catch (IOException e) {
    			log.error("Could not convert to PDF", e);
    		}
    	}
    	
    	return false;
    }
}
