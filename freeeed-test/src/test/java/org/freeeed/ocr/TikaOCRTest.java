package org.freeeed.ocr;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nehaojha on 03/01/18.
 */
public class TikaOCRTest {

    @Test
    @Ignore
    public void testOcrTikaParsing00() throws Exception {
        //read contents from 00 pdf and compare with expected text
        File file = new File("test-data/ocr/00.pdf");
        String text = parseToString(file);
        System.out.println("text = " + text);
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("test-data/ocr/00.txt"));
        System.out.println("Words matching: " + match);
    }

    @Test
    @Ignore
    public void testOcrTikaParsing01() throws Exception {
        //read contents from 01 pdf and compare with expected text
        File file = new File("test-data/ocr/01.pdf");
        String text = parseToString(file);
        System.out.println("text = " + text);
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("test-data/ocr/01.txt"));
        System.out.println("Words matching: " + match);
    }

    private String parseToString(File file) throws IOException, SAXException, TikaException {
        InputStream stream = new FileInputStream(file);

        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);

        TesseractOCRConfig config = new TesseractOCRConfig();
        PDFParserConfig pdfConfig = new PDFParserConfig();
        ParseContext parseContext = new ParseContext();

        parseContext.set(TesseractOCRConfig.class, config);
        parseContext.set(PDFParserConfig.class, pdfConfig);
        parseContext.set(Parser.class, parser); // need to add this to make sure recursive parsing happens!
        parser.parse(stream, handler, new Metadata(), parseContext);
        return handler.toString().trim();
    }
}
