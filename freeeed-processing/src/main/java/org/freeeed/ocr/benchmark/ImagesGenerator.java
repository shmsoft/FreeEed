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
package org.freeeed.ocr.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * Class ImagesGenerator.
 *
 * Generate number of images using a given text generator.
 *
 * @author ilazarov
 *
 */
@Deprecated
public class ImagesGenerator {

    private String templatesZip;
    private String templatesDir;
    private String outputDir;

    public ImagesGenerator(String templatesZip, String templatesDir, String outputDir) {
        this.templatesDir = templatesDir;
        this.templatesZip = templatesZip;
        this.outputDir = outputDir;
    }

    public void generate() throws IOException {
        File outputDirFile = new File(outputDir);
        String textsDir = outputDir + File.separator + "texts" + File.separator;

        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
            File textsDirFile = new File(textsDir);
            textsDirFile.mkdirs();
        } else {
            if (outputDirFile.list().length > 0) {
                System.out.println("Output directory not empty, will skip generation...");
                return;
            }
        }

        ITextGenerator textGenerator = new BrownCorpusTextGenerator(templatesZip, templatesDir);
        int filesCount = textGenerator.getTextsCount();

        System.out.println("Generating images: " + filesCount);

        for (int i = 0; i < filesCount; i++) {
            String text = textGenerator.getText(i);

            String file = outputDir + File.separator + i + ".png";
            ImageUtil.createImage(text, file, "png");

            String textFile = textsDir + i + ".png";
            FileWriter fw = new FileWriter(textFile);
            fw.write(text);
            fw.close();
        }
        
        System.out.println("Generating images - Done");
    }
}
