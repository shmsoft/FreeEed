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

import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
        if (!simpleParse.trim().isEmpty()) {
            return simpleParse;
        }
        LOGGER.info("processing pdf with ocr");
        return parseImages(file);
    }

    private static String parseImages(String file) {
        try (InputStream stream = new FileInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);
            AUTO_DETECT_PARSER.parse(stream, handler, new Metadata(), PARSE_CONTEXT);
            return handler.toString().trim();
        } catch (Exception ex) {
            LOGGER.error("Problem parsing document {}", file, ex);
        }
        return EMPTY;
    }

    private static String parseText(String filePath) {
        try {
            return TIKA.parseToString(new File(filePath));
        } catch (Exception ex) {
            LOGGER.error("Exception parsing pdf file ", ex);
        }
        return EMPTY;
    }
}
