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

import org.freeeed.services.Settings;

/**
 *
 * Class TesseractConfiguration.
 *
 * @author ilazarov
 *
 */
public class OCRConfiguration {

    private static final String TESS_BIN = "tesseract";
    private static final String TESS_WORK_DIR = Settings.getSettings().getTmpDir() + "/tesseract/";    
    private static final String PDF_IMAGE_EXTRACTION_DIR = Settings.getSettings().getTmpDir() + "tesseract/";            
    private static final String TESS_OUT_EXT = "txt";
    private String tesseractBin;
    private String tesseractWorkDir;
    private String tesseractOutputExtension;
    private String pdfImageExtractionDir;

    public OCRConfiguration() {
    	this(TESS_BIN, TESS_WORK_DIR, TESS_OUT_EXT, PDF_IMAGE_EXTRACTION_DIR);
    }

    public OCRConfiguration(String tesseractWorkDir) {
    	this(TESS_BIN, tesseractWorkDir, TESS_OUT_EXT, tesseractWorkDir);
    }
    
    public OCRConfiguration(String tesseractBin, String tesseractWorkDir, 
    		String tesseractOutputExtension, String pdfImageExtractionDir) {
        this.tesseractBin = tesseractBin;
        this.tesseractWorkDir = tesseractWorkDir + "/";
        this.tesseractOutputExtension = tesseractOutputExtension;
        this.pdfImageExtractionDir = pdfImageExtractionDir + "/";
    }

    public String getTesseractBin() {
        return tesseractBin;
    }

    public void setTesseractBin(String tesseractBin) {
        this.tesseractBin = tesseractBin;
    }

    public String getTesseractWorkDir() {
        return tesseractWorkDir;
    }

    public void setTesseractWorkDir(String tesseractWorkDir) {
        this.tesseractWorkDir = tesseractWorkDir;
    }

    public String getTesseractOutputExtension() {
        return tesseractOutputExtension;
    }

    public void setTesseractOutputExtension(String tesseractOutputExtension) {
        this.tesseractOutputExtension = tesseractOutputExtension;
    }

    public String getPdfImageExtractionDir() {
        return pdfImageExtractionDir;
    }

    public void setPdfImageExtractionDir(String pdfImageExtractionDir) {
        this.pdfImageExtractionDir = pdfImageExtractionDir;
    }
}
