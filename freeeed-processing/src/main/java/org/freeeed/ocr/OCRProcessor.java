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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.ocr.tess.TesseractOCRFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class OCRProcessor.
 *
 * Search in documents for images, do OCR processing over them and extract their text. Return the result text for each
 * image found.
 *
 * @author ilazarov
 *
 */
public class OCRProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OCRProcessor.class);
    private static OCRProcessor __instance;
    private OCREngine ocrEngine;
    private OCRConfiguration conf;

    /**
     *
     * Get the text for all images that are found in this document.
     *
     * @param documentFile
     * @return
     */
    public List<String> getImageText(String documentFile) {
        List<String> imageTexts = new ArrayList<>();

        if (!ocrEngine.isEngineAvailable()) {
            return imageTexts;
        }

        logger.trace("OCR - processing document: {}", documentFile);

        Document doc = Document.createDocument(documentFile, conf);

        if (doc.containImages()) {
            List<String> images = doc.getImages();

            logger.trace("OCR - Images: {}", images);

            for (String image : images) {
                String text = ocrEngine.getImageText(image);
                if (text != null) {
                    imageTexts.add(text);
                }

                if (conf.getContext() != null) {
                    conf.getContext().progress();
                }
            }
        }

        return imageTexts;
    }

    /**
     *
     * Creates an OCR processor with the given working directory.
     *
     * @param workDir
     * @param context
     * @return
     */
    public synchronized static OCRProcessor createProcessor(String workDir, Context context) {
        OCRConfiguration conf = new OCRConfiguration(workDir, context);
        return createProcessor(conf);
    }

    /**
     *
     * Creates an OCR processor with the given OCR configuration object.
     *
     * @param conf
     * @return
     */
    public synchronized static OCRProcessor createProcessor(OCRConfiguration conf) {
        if (__instance == null) {
            __instance = new OCRProcessor();
            __instance.setConf(conf);

            OCREngine ocrEngine = TesseractOCRFactory.createTesseractOCR(conf);
            __instance.setOcrEngine(ocrEngine);
        }

        return __instance;
    }

    public void setConf(OCRConfiguration conf) {
        this.conf = conf;
    }

    public void setOcrEngine(OCREngine ocrEngine) {
        this.ocrEngine = ocrEngine;
    }
}
