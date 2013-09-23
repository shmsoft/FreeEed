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

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * Class TesseractConfiguration.
 *
 * @author ilazarov
 *
 */
public class OCRConfiguration {

    private static final String TESS_BIN = "tesseract";
    private static final String TESS_WORK_DIR = ParameterProcessing.TMP_DIR + "/tesseract/";    
    private static final String PDF_IMAGE_EXTRACTION_DIR = ParameterProcessing.TMP_DIR + "tesseract/";            
    private static final String TESS_OUT_EXT = "txt";
    private String tesseractBin;
    private String tesseractWorkDir;
    private String tesseractOutputExtension;
    private String pdfImageExtractionDir;
    private Context context;

    public OCRConfiguration() {
    	this(TESS_BIN, TESS_WORK_DIR, TESS_OUT_EXT, PDF_IMAGE_EXTRACTION_DIR, null);
    }

    public OCRConfiguration(String tesseractWorkDir, Context context) {
    	this(TESS_BIN, tesseractWorkDir, TESS_OUT_EXT, tesseractWorkDir, context);
    }
    
    public OCRConfiguration(String tesseractBin, String tesseractWorkDir, 
    		String tesseractOutputExtension, String pdfImageExtractionDir, Context context) {
        this.tesseractBin = tesseractBin;
        this.tesseractWorkDir = tesseractWorkDir + "/";
        this.tesseractOutputExtension = tesseractOutputExtension;
        this.pdfImageExtractionDir = pdfImageExtractionDir + "/";
        this.context = context;
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
