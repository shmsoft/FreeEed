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

/**
 * 
 * Interface OCREngine.
 * 
 * Provide common interface for all OCR engines that are used.
 * 
 * @author ilazarov
 *
 */

public interface OCREngine {

    /**
     * Do OCR processing over the given image file
     * and return the recognized text. 
     * 
     * @param imageFile
     * @return
     */
    String getImageText(String imageFile);
    
    /**
     * Check if the OCR engine is available.
     * This should encapsulate all platform specific
     * checks.
     */
    boolean isEngineAvailable();
}
