package org.freeeed.print;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfWriter;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import org.freeeed.main.PlatformUtil;

public class Html2Pdf {

    public static void html2pdf(String inputFile, String outputFile) {
        if ((PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.LINUX) || (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.MACOSX)) {
            html2pdfwk(inputFile, outputFile);
        } else {
            try {
                html2pdf_itext(inputFile, outputFile);
            } catch (Exception e) {
                System.out.println("html2pdf warning: " + e.getMessage());
            }
        }
    }

    /**
     * Bad rendering, perhaps used only for Windows
     */
    public static void html2pdf_itext(String inputFile, String outputFile) throws Exception {
        Document pdfDocument = new Document();
        Reader htmlreader = new BufferedReader(new InputStreamReader(
                new FileInputStream(inputFile)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdfDocument, baos);
        pdfDocument.open();
        StyleSheet styles = new StyleSheet();
        styles.loadTagStyle("body", "font", "Times New Roman");
        ArrayList arrayElementList = HTMLWorker.parseToList(htmlreader, styles);
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
     * wkhtmltopdf needs to be installed
     * It is a great utility under active development
     * It uses X11 and WebKit rendering engine of Apple's Safari     
     */
    public static void html2pdfwk(String inputFile, String outputFile) {
        String command = "wkhtmltopdf " + inputFile + " " + outputFile;
        PlatformUtil.runUnixCommand(command);
    }
}
