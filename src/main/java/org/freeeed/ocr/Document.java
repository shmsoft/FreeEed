/*    
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.freeeed.services.History;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 *
 * Class Document.
 *
 * Provide methods for detecting if a given file is an image, or if the provided
 * document contains images.
 *
 * @author ilazarov
 *
 */
public class Document {

    public static enum DocumentType {

        PDF,
        IMAGE,
        UNKNOWN
    }
    private DocumentType type = DocumentType.UNKNOWN;
    private String file;
    private List<String> images;
    private Metadata metadata;
    private OCRConfiguration conf;

    /**
     * Create an image parser for the given file.
     *
     * @param file
     */
    private Document(String file, OCRConfiguration conf) {
        this.file = file;
        this.conf = conf;

        parseContent();
        detectType();
        extractImages();
    }

    private void parseContent() {
        metadata = new Metadata();

        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            AutoDetectParser parser = new AutoDetectParser();
            metadata.add(TikaMetadataKeys.RESOURCE_NAME_KEY, file);

            parser.parse(stream, new DefaultHandler(), metadata, new ParseContext());
            stream.close();

        } catch (Exception e) {
            History.appendToHistory("Exception: " + e.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private void detectType() {
        String mimeType = metadata.get(HttpHeaders.CONTENT_TYPE);
        if (mimeType == null) {
            return;
        }

        if (mimeType.contains("image")) {
            this.type = DocumentType.IMAGE;
        } else if (mimeType.contains("pdf")) {
            this.type = DocumentType.PDF;
        } else {
            this.type = DocumentType.UNKNOWN;
        }
    }

    private void extractImages() {
        this.images = new ArrayList<String>();

        if (type == DocumentType.IMAGE) {
            images.add(file);
            return;
        }

        ImageExtractor imageExtractor = ImageExtractor.createImageExtractor(type, file, conf);
        if (imageExtractor != null) {
            images = imageExtractor.extractImages();
        }
    }

    /**
     * Return all extracted images, if any
     *
     * @return
     */
    public List<String> getImages() {
        return images;
    }

    /**
     * Return true if this file contains images
     *
     * @return
     */
    public boolean containImages() {
        return type == DocumentType.IMAGE || images.size() > 0;
    }

    /**
        *
     * Create a new image parser for the given image.
     *
     * @param file
     * @param conf
     * @return
     */
    public static Document createDocument(String file, OCRConfiguration conf) {
        Document doc = new Document(file, conf);
        return doc;
    }
}
