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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class OCRProcessor.
 * <p>
 * Search in documents for images, do OCR processing over them and extract their text. Return the result text for each
 * image found.
 *
 * @author ilazarov
 */
public class OCRProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OCRProcessor.class);

    /**
     * Get the text for all images that are found in this document.
     *
     * @param documentFile
     * @return
     */
    public String getImageText(String documentFile) {
        LOGGER.trace("OCR - processing document: {}", documentFile);
        return ImageTextParser.parseContent(documentFile);
    }
}
