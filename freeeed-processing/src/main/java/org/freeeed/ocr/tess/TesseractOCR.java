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
package org.freeeed.ocr.tess;

import java.io.File;

import org.freeeed.ocr.OCRConfiguration;
import org.freeeed.ocr.OCREngine;
import org.freeeed.ocr.OCRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class TesseractOCR.
 *
 * @author ilazarov
 *
 */
public class TesseractOCR implements OCREngine {
    private static final Logger logger = LoggerFactory.getLogger(TesseractOCR.class);
    private Boolean tesseractAvailable;
    private TesseractAdapter tessAdapter;
    private OCRConfiguration configuration;

    protected TesseractOCR() {
        //hiding the constructor. Get and instance using the factory.
    }

    @Override
    public String getImageText(String imageFile) {
        try {
            File outDir = new File(configuration.getTesseractWorkDir());
            outDir.mkdirs();

            String output = configuration.getTesseractWorkDir() + OCRUtil.createUniqueFileName("out");
            tessAdapter.call(imageFile, output);

            String outputFile = output + "."
                    + configuration.getTesseractOutputExtension();

            logger.trace("TesseractOCR - processing, outputFile: {} waiting...", outputFile);

            File resultFile = new File(outputFile);
            // as the creation of the file goes in OS background, wait for the result
            int maxRetries = 10;
            while (!resultFile.exists() && maxRetries > 0) {
                // sleep 1 second
                try {
                    Thread.sleep(500);
                    maxRetries--;
                } catch (InterruptedException e) {
                    logger.trace("Interrupted: ", e);
                }
            }

            if (!resultFile.exists()) {
                //logger.warn("TesseractOCR - image file not recognized");
                return null;
            }

            logger.trace("TesseractOCR - file processed: {}", outputFile);

            return OCRUtil.readFileContent(outputFile);
        } catch (Exception e) {
            logger.error("TesseractOCR - Problem processing image: {}", imageFile, e);
        }

        return null;
    }

    @Override
    public boolean isEngineAvailable() {
        synchronized (this) {
            if (tesseractAvailable == null) {
                tesseractAvailable = tessAdapter.verifyTesseract();
            }

            return tesseractAvailable;
        }
    }

    public void setTessAdapter(TesseractAdapter tessAdapter) {
        this.tessAdapter = tessAdapter;
    }

    public void setConfiguration(OCRConfiguration configuration) {
        this.configuration = configuration;
    }
}
