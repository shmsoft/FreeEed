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

import com.lowagie.text.BadElementException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.PlatformUtil;

import com.lowagie.text.DocListener;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.html.simpleparser.ChainedProperties;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.ImageProvider;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Html2Pdf {
    private static Logger logger = LoggerFactory.getLogger(Html2Pdf.class);
    public static void html2pdf(String inputFile, String outputFile) throws Exception {
        html2pdf_itext(inputFile, outputFile);
    }

    public static void htmlContent2Pdf(String inputContent, String outputFile) throws Exception {
        StringReader htmlReader = new StringReader(inputContent);
        convertHtml2Pdf(htmlReader, outputFile);
    }

    private static void html2pdf_itext(String inputFile, String outputFile) throws Exception {
        Reader htmlreader = new BufferedReader(new InputStreamReader(
                new FileInputStream(inputFile)));
        convertHtml2Pdf(htmlreader, outputFile);
    }

    /**
     * Bad rendering, perhaps used only for Windows
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void convertHtml2Pdf(Reader htmlReader, String outputFile) throws Exception {
        Document pdfDocument = new Document();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdfDocument, baos);
        pdfDocument.open();
        StyleSheet styles = new StyleSheet();
        styles.loadTagStyle("body", "font", "Times New Roman");

        ImageProvider imageProvider = new ImageProvider() {
            @Override
            public Image getImage(String src, HashMap arg1,
                    ChainedProperties arg2, DocListener arg3) {

                try {
                    Image image = Image.getInstance(IOUtils.toByteArray(
                            getClass().getClassLoader().getResourceAsStream(ParameterProcessing.NO_IMAGE_FILE)));
                    return image;
                } catch (IOException | BadElementException e) {
                    logger.warn("Problem with html to pdf rendering.", e);
                }

                return null;
            }
        };

        HashMap interfaceProps = new HashMap();
        interfaceProps.put("img_provider", imageProvider);

        ArrayList arrayElementList = HTMLWorker.parseToList(htmlReader, styles, interfaceProps);
        for (int i = 0; i < arrayElementList.size(); ++i) {
            Element e = (Element) arrayElementList.get(i);
            pdfDocument.add(e);
        }
        pdfDocument.close();
        byte[] bs = baos.toByteArray();
        File pdfFile = new File(outputFile);
        FileOutputStream out = new FileOutputStream(pdfFile);
        out.write(bs);
        out.close();
    }

    /**
     * wkhtmltopdf needs to be installed It is a great utility under active development It uses X11 and WebKit rendering
     * engine of Apple's Safari
     */
    public static void html2pdfwk(String inputFile, String outputFile) {
        String command = "wkhtmltopdf " + inputFile + " " + outputFile;
        PlatformUtil.runCommand(command);
    }
}
