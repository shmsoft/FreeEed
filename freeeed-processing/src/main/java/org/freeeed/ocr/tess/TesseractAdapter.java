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

import java.util.List;

import org.freeeed.main.PlatformUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Class TesseractAdapter.
 *
 * Call OS specific command for tesseract.
 *
 * @author ilazarov
 *
 */
public class TesseractAdapter {

    private static final Logger logger = LoggerFactory.getLogger(TesseractAdapter.class);
    private static final String TESSERACT_VERSION_LINE = "tesseract 3.";
    private static TesseractAdapter __instance;
    private String tesseractBin;
    
    private TesseractAdapter(String tesseractBin) {
        this.tesseractBin = tesseractBin;
    }
    
    public static synchronized TesseractAdapter createTesseract(String tesseractBin) {
        if (__instance == null) {
            __instance = new TesseractAdapter(tesseractBin);
        }
        
        return __instance;
    }

    /**
     * Call the tesseract bin to extract the image file text.
     *
     * @param imageFile
     * @param outputPath
     * @return
     */
    public List<String> call(String imageFile, String outputPath) {
        return PlatformUtil.runUnixCommand(tesseractBin + " " + escapeImageName(imageFile) + " " + outputPath);
    }

    /**
     *
     * Verify that the tesseract application is installed and has a proper version.
     *
     * @return true if tesseract is installed, false if not.
     */
    public boolean verifyTesseract() {
        if (PlatformUtil.isNix()) {
            List<String> output = PlatformUtil.runUnixCommand(tesseractBin + " -v", true);
            for (String line : output) {
                if (line.startsWith(TESSERACT_VERSION_LINE)) {
                    logger.info("Tesseract installed is confirmed");
                    return true;
                }
            }
        }        
        logger.error("Tesseract is not installed, but it is required");
        return false;
    }
    
    private static String escapeImageName(String imageName) {
        return imageName.replace(" ", "\\ ").replace("!", "\\!");
    }
}
