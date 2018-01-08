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

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Class Document.
 * <p>
 * Provide methods for detecting if a given file is an image, or if the provided document contains images.
 *
 * @author ilazarov
 */
public class Document {

    private static final Logger LOGGER = LoggerFactory.getLogger(Document.class);

    public static String parseContent(String file) {
        try (InputStream stream = new FileInputStream(file)) {
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(Integer.MAX_VALUE);

            TesseractOCRConfig config = new TesseractOCRConfig();
            PDFParserConfig pdfConfig = new PDFParserConfig();
            pdfConfig.setExtractInlineImages(true);
            ParseContext parseContext = new ParseContext();

            parseContext.set(TesseractOCRConfig.class, config);
            parseContext.set(PDFParserConfig.class, pdfConfig);
            parseContext.set(Parser.class, parser); // need to add this to make sure recursive parsing happens!
            parser.parse(stream, handler, new Metadata(), parseContext);
            return handler.toString().trim();
        } catch (Exception ex) {
            LOGGER.warn("Problem parsing document {}", file, ex);
        }
        return "";
    }

}
