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
import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.freeeed.data.index.ComponentLifecycle;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.History;


import com.google.common.io.Files;

import de.schlichtherle.io.FileOutputStream;

public class OfficePrint implements ComponentLifecycle {

    private static OfficePrint instance;
    private OfficeManager officeManager;

    public static synchronized OfficePrint getInstance() {
        if (instance == null) {
            instance = new OfficePrint();
        }
        return instance;
    }

    public void createPdf(String officeDocFile, String outputPdf, String originalFileName) {
        String extension = FreeEedUtil.getExtension(officeDocFile);
        if (extension == null || extension.isEmpty()) {
            extension = FreeEedUtil.getExtension(originalFileName);
        }

        try {
            if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
                try {
                    Html2Pdf.html2pdf(officeDocFile, outputPdf);
                } catch (Exception e) {
                    System.out.println("Warning: cannot html convert: " + e.getMessage());
                    ooConvert(officeDocFile, outputPdf);
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
                ooConvert(officeDocFile, outputPdf);
                return;
            }
        } catch (Exception e) {
            History.appendToHistory("Problem creating PDF file for:"
                    + officeDocFile);
        }

        try {
            IOUtils.copy(getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_PDF_IMAGE_FILE),
                    new FileOutputStream(outputPdf));
        } catch (IOException e) {
            System.out.println("Problem with default imaging");
        }
    }

    private void convertToPDFUsingHtml(String officeDocFile, String outputPdf, EmailDataProvider emlParser) {
        try {
            String emlHtmlContent = EmailUtil.createHtmlFromEmlFile(officeDocFile, emlParser);
            Html2Pdf.htmlContent2Pdf(emlHtmlContent, outputPdf);
        } catch (Exception e) {
            System.out.println("Warning: cannot convert eml file: " + e.getMessage());
            ooConvert(officeDocFile, outputPdf);
        }
    }

    private void ooConvert(String officeDocFile, String outputPdf) {
        if (officeManager != null) {
            OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
            converter.convert(new File(officeDocFile), new File(outputPdf));

        } else {
            throw new RuntimeException("No open office installed!");
        }
    }

    @Override
    public void init() {
        try {
            officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
            officeManager.start();
        } catch (Exception e) {
            History.appendToHistory("Open office not installed.");
            System.out.println("Warn: Problem connecting to Open office" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (officeManager != null) {
            officeManager.stop();
        }
    }
}
