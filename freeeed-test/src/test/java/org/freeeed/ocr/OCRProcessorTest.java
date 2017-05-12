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

import java.io.File;
import java.io.IOException;
import java.util.List;
// needed for @Test
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OCRProcessorTest {
    // Uncomment if you want to test OCR (but it takes a few minutes)
    // @Test
    public void testGetImageText() {
        OCRConfiguration conf = new OCRConfiguration();
        conf.setPdfImageExtractionDir("output/ocr/out/");
        conf.setTesseractWorkDir("output/ocr/out/");

        File f = new File("output/ocr/out");
        f.mkdirs(); 
        long start = System.currentTimeMillis();

        OCRProcessor processor = OCRProcessor.createProcessor(conf);
        List<String> data = processor.getImageText("test-data/02-loose-files/docs/ocr/516.pdf");

        long end = System.currentTimeMillis();

        assertEquals(4, data.size());

        double match = 100;
        try {
            match = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/02-loose-files/docs/ocr/516.txt"));
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Unexpected exception");
        }

        System.out.println("516.pdf = Time: " + (end - start));
        System.out.println("516.pdf = Words matching: " + match);

        start = System.currentTimeMillis();

        data = processor.getImageText("test-data/02-loose-files/docs/ocr/testb.pdf");

        end = System.currentTimeMillis();

        assertEquals(2, data.size());

        double match1 = 100;
        double match2 = 100;
        try {
            match1 = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent("test-data/02-loose-files/docs/ocr/testb_1.txt"));
            match2 = OCRUtil.compareText(data.get(1), OCRUtil.readFileContent("test-data/02-loose-files/docs/ocr/testb_2.txt"));
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Unexpected exception");
        }

        System.out.println("testb.pdf = Time: " + (end - start));
        System.out.println("testb.pdf 1 = Words matching: " + match1);
        System.out.println("testb.pdf 2 = Words matching: " + match2);
    }
}
