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
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.office.OfficeUtils;
import org.artofsolving.jodconverter.util.PlatformUtils;
import org.freeeed.data.index.ComponentLifecycle;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import de.schlichtherle.io.FileOutputStream;

/**
 * Document conversions
 * LibreOffice documentation pointers
 * https://ask.libreoffice.org/en/question/2641/convert-to-command-line-parameter/
 * @author mark
 */
public class OfficePrint implements ComponentLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(OfficePrint.class);

    private static OfficePrint instance;
    private OfficeManager officeManager;

    private OfficePrint() {
        setup();
    }

    public static synchronized OfficePrint getInstance() {
        if (instance == null) {
            instance = new OfficePrint();
        }
        return instance;
    }

    public void createPdf(String officeDocFile, String outputPdf, String originalFileName) {
        String extension = Util.getExtension(officeDocFile);
        if (extension == null || extension.isEmpty()) {
            extension = Util.getExtension(originalFileName);
        }

        try {
            if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
                try {
                    Html2Pdf.html2pdf(officeDocFile, outputPdf);
                } catch (Exception e) {
                    logger.info("htmltopdf imaging not able to process file, trying OpenOffice imaging instead", e.getMessage());
                    OfficeDocConvert(officeDocFile, outputPdf);
                }

                return;
            } else if ("pdf".equalsIgnoreCase(extension)) {
                Files.copy(new File(officeDocFile), new File(outputPdf));

                return;
            } else if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(new File(officeDocFile));
                convertToPDFUsingHtml(officeDocFile, outputPdf, emlParser);

                return;
            } else if ("nsfe".equalsIgnoreCase(extension)) {
                NSFXDataParser emlParser = new NSFXDataParser(new File(officeDocFile));
                convertToPDFUsingHtml(officeDocFile, outputPdf, emlParser);

                return;
            } else {
                OfficeDocConvert(officeDocFile, outputPdf);
                return;
            }
        } catch (Exception e) {
            logger.error("Problem creating PDF file for: {}", officeDocFile, e);
        }

        try {
            IOUtils.copy(getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_PDF_IMAGE_FILE),
                    new FileOutputStream(outputPdf));
        } catch (IOException e) {
            logger.error("Problem with default imaging", e);
        }
    }

    private void convertToPDFUsingHtml(String officeDocFile, String outputPdf, EmailDataProvider emlParser) {
        try {
            String emlHtmlContent = EmailUtil.createHtmlFromEmlFile(officeDocFile, emlParser);
            Html2Pdf.htmlContent2Pdf(emlHtmlContent, outputPdf);
        } catch (Exception e) {
            logger.error("Cannot convert eml file: {}", e.getMessage());
            OfficeDocConvert(officeDocFile, outputPdf);
        }
    }

    public void OfficeDocConvert(String officeDocFile, String output) {
        // this should be simply
        // libreoffice --headless --convert-to pdf document.doc
        if (officeManager != null) {
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
            converter.convert(new File(officeDocFile), new File(output));
        } else {
            throw new RuntimeException("Open office is not installed");
        }
    }

    private void setup() {
        logger.info("Init Office Print...");
        try {
            File defaultOfficeHome = OfficeUtils.getDefaultOfficeHome();
            if (defaultOfficeHome == null) {
                logger.info("Cannot find the default OO home directory...");
                String oofficeSetting = Settings.getSettings().getOpenOfficeHome();
                if (!StringUtils.isEmpty(oofficeSetting)) {
                    logger.info("Open office home defined as setting: " + oofficeSetting);
                    defaultOfficeHome = new File(oofficeSetting);
                } else if (PlatformUtils.isWindows()) {
                    defaultOfficeHome = new File(System.getenv("ProgramFiles"), "OpenOffice 4");
                    if (!defaultOfficeHome.exists()) {
                        defaultOfficeHome = new File(System.getenv("ProgramFiles(x86)"), "OpenOffice 4");
                    }
                } else {
                    defaultOfficeHome = new File("/opt/openoffice.org4");
                }
            }
            logger.info("Will use as open office home: " + defaultOfficeHome);
            DefaultOfficeManagerConfiguration configuration = new DefaultOfficeManagerConfiguration();
            configuration.setOfficeHome(defaultOfficeHome);

            officeManager = configuration.buildOfficeManager();
        } catch (NullPointerException | IllegalArgumentException | IllegalStateException | OfficeException e) {
            logger.error("Problem connecting to Open office", e);
            logger.info("If you are having problems with OpenOffice - install it, and set its home in the settings.properties");
        }
    }

    public void init() {
        try {
            officeManager.start();
        } catch (Exception e) {
            logger.error("Problem starting OO manager: {}", e.getMessage());
        }
    }

    public void destroy() {
        try {
            if (officeManager != null) {
                officeManager.stop();
            }
        } catch (Exception e) {
            logger.error("Problem stoping OO manager: {}", e.getMessage());
        }
    }
}
