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
package org.freeeed.ocr.tess;

import org.freeeed.ocr.OCRConfiguration;

/**
 *
 * Class TesseractOCRFactory.
 *
 * Factory for creating TesseractOCR objects.
 *
 * @author ilazarov.
 *
 */
public class TesseractOCRFactory {
    
    //keep a single object
    private static TesseractOCR tesseractOCR;

    /**
     *
     * Create a TesseractOCR instance.
     *
     * @param conf
     * @return
     */
    public static synchronized TesseractOCR createTesseractOCR(OCRConfiguration conf) {
        if (tesseractOCR == null) {
            TesseractAdapter tessAdapter = TesseractAdapter.createTesseract(conf.getTesseractBin());
            tesseractOCR = new TesseractOCR();
            tesseractOCR.setConfiguration(conf);
            tesseractOCR.setTessAdapter(tessAdapter);
        }

        return tesseractOCR;
    }
}
