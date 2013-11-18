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

import java.util.List;

import org.freeeed.ocr.Document.DocumentType;

/**
 *
 * Class ImageExtractor.
 *
 * Super class for all image extractors based on their type.
 *
 * @author ilazarov
 *
 */
public abstract class ImageExtractor {

    protected String file;
    protected OCRConfiguration conf;

    protected ImageExtractor(String file) {
        this.file = file;
    }

    /**
     * Extract and return the images for this file, if any
     *
     * @return
     */
    public abstract List<String> extractImages();

    /**
     *
     * Create a image extractor based on the document type.
     *
     * @param type
     * @param file
     * @return
     */
    public static ImageExtractor createImageExtractor(DocumentType type, String file, OCRConfiguration conf) {
        switch (type) {
            case PDF: {
                ImageExtractor imageExtractor = new PDFImageExtractor(file);
                imageExtractor.setConf(conf);

                return imageExtractor;
            }
            default:
                return null;
        }
    }

    public void setConf(OCRConfiguration conf) {
        this.conf = conf;
    }
}
