package org.freeeed.html;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
//import org.apache.poi.xwpf.usermodel.XWPFDocument;
//import org.apache.poi.xwpf.usermodel.XWPFWordExtractor;
//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;

import javax.imageio.ImageIO;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;

public class FileToHtmlPreview {

    public static void main(String[] args) throws Exception {
        // Example file paths
        String pdfFilePath = "example.pdf";
        String wordFilePath = "example.docx";
        String emlFilePath = "example.eml";

        // Convert files and generate HTML
        convertPdfToHtml(pdfFilePath);
        convertWordToHtml(wordFilePath);
        convertEmlToHtml(emlFilePath);
    }

    public static void convertPdfToHtml(String pdfFilePath) throws Exception {
        PDDocument document = PDDocument.load(new File(pdfFilePath));
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300); // Render the first page
        String outputImagePath = "output.png";
        ImageIO.write(bim, "png", new File(outputImagePath));
        document.close();
        generateHtmlPreview("PDF Preview", outputImagePath);
    }

    public static void convertWordToHtml(String wordFilePath) throws Exception {
        FileInputStream fis = new FileInputStream(wordFilePath);
        BufferedImage image = null;
//        if (wordFilePath.endsWith(".docx")) {
//            XWPFDocument document = new XWPFDocument(fis);
//            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
//            // Save text to an image (for simplicity, not implemented here)
//        } else if (wordFilePath.endsWith(".doc")) {
//            HWPFDocument document = new HWPFDocument(fis);
//            WordExtractor extractor = new WordExtractor(document);
//            // Save text to an image (for simplicity, not implemented here)
//        }
        // Assuming `image` is created and saved
        String outputImagePath = "output_word.png";
        ImageIO.write(image, "png", new File(outputImagePath));
        generateHtmlPreview("Word Preview", outputImagePath);
    }

    public static void convertEmlToHtml(String emlFilePath) throws Exception {
        Properties props = new Properties();
        Session mailSession = Session.getDefaultInstance(props, null);
        InputStream source = new FileInputStream(new File(emlFilePath));
        MimeMessage message = new MimeMessage(mailSession, source);
        Object content = message.getContent();

        if (content instanceof String) {
            // Save text to an image (for simplicity, not implemented here)
        } else if (content instanceof MimeMultipart) {
            MimeMultipart mimeMultipart = (MimeMultipart) content;
            Multipart mp = (Multipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bodyPart = mp.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    // Save text to an image (for simplicity, not implemented here)
                }
            }
        }
        // Assuming `image` is created and saved
        String outputImagePath = "output_eml.png";
        BufferedImage image = null; // Placeholder for the actual image conversion
        ImageIO.write(image, "png", new File(outputImagePath));
        generateHtmlPreview("EML Preview", outputImagePath);
    }

    public static void generateHtmlPreview(String title, String imagePath) throws Exception {
        String htmlContent = "<html><head><title>" + title + "</title></head><body>"
                + "<h1>" + title + "</h1>"
                + "<img src='" + imagePath + "' alt='Preview Image'/>"
                + "</body></html>";
        File htmlFile = new File("preview.html");
        try (FileWriter writer = new FileWriter(htmlFile)) {
            writer.write(htmlContent);
        }
    }
}
