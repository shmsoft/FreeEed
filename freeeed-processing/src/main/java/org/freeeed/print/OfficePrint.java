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
import org.freeeed.data.index.ComponentLifecycle;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.io.IOUtils;

import com.google.common.io.Files;

import de.schlichtherle.io.FileOutputStream;
import org.freeeed.util.OsUtil;

public class OfficePrint implements ComponentLifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(OfficePrint.class);

    private static OfficePrint instance;

    private OfficePrint() {
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
                    LOGGER.info("htmltopdf imaging not able to process file, trying OpenOffice imaging instead", e.getMessage());
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
            LOGGER.error("Problem creating PDF file for: {}", officeDocFile, e);
        }

        try {
            IOUtils.copy(getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_PDF_IMAGE_FILE),
                    new FileOutputStream(outputPdf));
        } catch (IOException e) {
            LOGGER.error("Problem with default imaging", e);
        }
    }

    private void convertToPDFUsingHtml(String officeDocFile, String outputPdf, EmailDataProvider emlParser) {
        try {
            String emlHtmlContent = EmailUtil.createHtmlFromEmlFile(officeDocFile, emlParser);
            Html2Pdf.htmlContent2Pdf(emlHtmlContent, outputPdf);
        } catch (Exception e) {
            LOGGER.error("Cannot convert eml file: {}", e.getMessage());
            OfficeDocConvert(officeDocFile, outputPdf);
        }
    }

    public void OfficeDocConvert(String officeDocFile, String output) {
        String command = "libreoffice --headless --convert-to pdf document.doc";
        OsUtil.runCommand(command);
        // TODO output into a different file name?
        // TODO - check results simply by verifying that the output is there
        // return status, throw exception or ???
    }
}
