package org.freeeed.html;

import java.io.*;
import java.nio.charset.Charset;

import org.freeeed.mail.EmailUtil;
import org.freeeed.mail.EmlParser;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.Util;

import com.google.common.io.Files;

/**
 *
 * Class DocumentToHtml.
 *
 * Generate html from various type of documents
 *
 * @author ilazarov
 *
 */
public class DocumentToHtml {

    private static DocumentToHtml __instance;
    private static final String DEFAULT_HTML_CONTENT = "<div>No HTML available</div>";

    //singleton
    private DocumentToHtml() {
    }

    public static synchronized DocumentToHtml getInstance() {
        if (__instance == null) {
            __instance = new DocumentToHtml();
        }

        return __instance;
    }

    public void createHtml(File officeDocFile, File outputHtml, String originalFileName) throws Exception {
        String extension = Util.getExtension(officeDocFile.getPath());
        if (extension == null || extension.isEmpty()) {
            extension = Util.getExtension(originalFileName);
        }

        switch (extension)
        {
            case "txt":
                textToHtml(officeDocFile, outputHtml);
                break;
            case "eml":
                EmlParser emlParser = new EmlParser(officeDocFile);
                String emlHtmlContent = EmailUtil.createHtmlFromEmlFileNoCData(officeDocFile.getPath(), emlParser);
                Files.append(emlHtmlContent, outputHtml, Charset.defaultCharset());
                break;
            case "doc":
            case "docx":
                try {
                    OfficePrint.getInstance().convertToPdfWithSOffice(officeDocFile, outputHtml);
                } catch (Exception e) {
                    printDefaultHtml(outputHtml);
                }
                break;
            case "html":
            case "htm":
                Files.copy(officeDocFile, outputHtml);
                break;
            default:
                printDefaultHtml(outputHtml);
                break;
        }



    }
    private void textToHtml(File txtFile ,File outputHtml) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Wrap content with HTML tags
        String htmlContent = "<html>\n<body>\n<pre>\n" + content.toString() + "\n</pre>\n</body>\n</html>";

        // Write to HTML file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputHtml))) {
            bw.write(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void printDefaultHtml(File outputHtml) throws IOException {
        Files.write(DEFAULT_HTML_CONTENT, outputHtml, Charset.defaultCharset());
    }
}
