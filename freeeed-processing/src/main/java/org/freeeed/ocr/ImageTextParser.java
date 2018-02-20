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
package org.freeeed.ocr;

import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * This class parses file with or without embedded documents
 *
 * @author nehaojha
 */
public class ImageTextParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageTextParser.class);
    private static final Tika TIKA = new Tika();
    private static final Parser AUTO_DETECT_PARSER = new AutoDetectParser();
    private static final ParseContext PARSE_CONTEXT = new ParseContext();
    private static final String EMPTY = "";
    private static final Project CURRENT_PROJECT = Project.getCurrentProject();
    private static final String TMP = "/tmp/";
    private static final AtomicInteger COUNTER = new AtomicInteger();


    //config
    static {
        TesseractOCRConfig config = new TesseractOCRConfig();
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setExtractInlineImages(true);
        PARSE_CONTEXT.set(TesseractOCRConfig.class, config);
        PARSE_CONTEXT.set(PDFParserConfig.class, pdfConfig);
        PARSE_CONTEXT.set(Parser.class, AUTO_DETECT_PARSER);
    }

    public static String parseContent(String file) {
        String simpleParse = parseText(file);
        if (!simpleParse.trim().isEmpty() || !CURRENT_PROJECT.isOcrEnabled()) {
            return simpleParse;
        }
        LOGGER.info("processing pdf with ocr");
        return parseImages(file);
    }

    private static String parseText(String filePath) {
        try {
            return TIKA.parseToString(new File(filePath));
        } catch (Exception ex) {
            LOGGER.error("Exception parsing pdf file ", ex);
        }
        return EMPTY;
    }

    private static String parseImages(String file) {
        //break file into pages, do ocr and then send parsed contents
        try {
            String pagePath = splitPages(file);
            return ocrParallel(pagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EMPTY;
    }

    private static String ocrParallel(String pagePath) {
        File root = new File(pagePath);
        File[] files = root.listFiles();
        String[] contents = new String[files.length];
        COUNTER.set(0);
        IntStream.rangeClosed(1, contents.length).parallel().forEach(i -> {
            File file = new File(pagePath + i + ".pdf");
            parseAndPut(contents, i, file);
        });
        return combineAll(contents);
    }

    private static void parseAndPut(String[] contents, int i, File file) {
        try (InputStream stream = new FileInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);
            AUTO_DETECT_PARSER.parse(stream, handler, new Metadata(), PARSE_CONTEXT);
            contents[i - 1] = handler.toString().trim();
            file.delete();
            int count = COUNTER.incrementAndGet();
            System.out.println("progress " + count + " of " + contents.length);
        } catch (Exception ex) {
            LOGGER.error("Problem parsing document {}", file, ex);
        }
    }

    private static String combineAll(String[] contents) {
        StringBuilder content = new StringBuilder();
        for (String str : contents) {
            content.append(str);
        }
        return content.toString();
    }

    private static String splitPages(String filePath) throws IOException {
        File file = new File(filePath);
        String pagePath;
        try (PDDocument document = PDDocument.load(file)) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(document);
            Iterator<PDDocument> iterator = pages.listIterator();
            int i = 1;
            pagePath = createTempPath(file);
            System.out.println("pagePath = " + pagePath);
            while (iterator.hasNext()) {
                PDDocument pd = iterator.next();
                pd.save(pagePath + i++ + ".pdf");
            }
        }
        return pagePath;
    }

    private static String createTempPath(File file) {
        String pagePath = TMP + file.getName() + "/";
        File tempFile = new File(pagePath);
        if (!tempFile.exists()) {
            tempFile.mkdirs();
        }
        return pagePath;
    }

}
