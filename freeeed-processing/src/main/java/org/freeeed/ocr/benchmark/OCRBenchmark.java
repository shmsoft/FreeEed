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
import java.util.ArrayList;
import java.util.List;

import org.freeeed.ocr.OCRConfiguration;
import org.freeeed.ocr.OCRProcessor;
import org.freeeed.ocr.OCRUtil;

public class OCRBenchmark {

    /**
     *
     * Run full test for OCR.
     *
     * @param templatesZip
     * @param templatesDir
     * @param outputDir
     * @param tesseractWorkDir
     */
    public void runFullTest(String templatesZip, String templatesDir,
            String outputDir, String tesseractWorkDir) {

        try {
            ImagesGenerator ig = new ImagesGenerator(templatesZip, templatesDir, outputDir);
            ig.generate();

            File f = new File(tesseractWorkDir);
            f.mkdirs();

            System.out.println("Starting OCR processing...");
            
            List<OCRResult> results = new ArrayList<OCRResult>();
            long totalStart = System.currentTimeMillis();

            File outputDirFile = new File(outputDir);
            String[] files = outputDirFile.list();
            System.out.println("Files: " + files.length);
            
            for (String file : files) {
                String imageFileName = outputDir + File.separatorChar + file;
                File imageFile = new File(imageFileName);
                if (imageFile.isDirectory()) {
                    continue;
                }

                String textFileName = outputDir + File.separatorChar + "texts" + File.separatorChar + file;

                OCRConfiguration conf = new OCRConfiguration(tesseractWorkDir);
                OCRProcessor processor = OCRProcessor.createProcessor(conf);

                long start = System.currentTimeMillis();
                List<String> data = processor.getImageText(imageFileName);
                long end = System.currentTimeMillis();

                try {
                    double match = OCRUtil.compareText(data.get(0), OCRUtil.readFileContent(textFileName));
                    results.add(new OCRResult(file, (end - start), match));
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
                
                System.out.print(".");
            }

            System.out.println("\nOCR processing - Done");
            
            long totalEnd = System.currentTimeMillis();
            printResults((totalEnd - totalStart), results, outputDir);

        } catch (IOException e) {
            System.out.println("Problem running the test: " + e.getMessage());
        }
    }

    private void printResults(long time, List<OCRResult> results, String outputDir) throws IOException {
        StringBuilder output = new StringBuilder();

        output.append("Total OCR processing time: ").append(time).append("\n");
        output.append("Results collected: ").append(results.size()).append("\n");

        double match = 0;
        for (OCRResult ocrResult : results) {
            output.append(ocrResult.file).append(",").append(ocrResult.time).append(",").append(ocrResult.match).append("\n");
            match += ocrResult.match;
        }

        double avgMatch = match / results.size();
        double avgTime = (double) time / results.size();

        output.append("Average matching result: ").append(avgMatch).append("\n");
        output.append("Average time result: ").append(avgTime).append("\n");

        String outputText = output.toString();
        System.out.println(outputText);

        FileWriter fw = new FileWriter(outputDir + File.separator + "output.csv");
        fw.write(outputText);
        fw.close();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String templatesZip = "";
        String templatesDir = "";
        String outputDir = "";
        String tesseractWorkDir = "";
        
    	try {
            templatesZip = args[0];
            templatesDir = args[1];
            outputDir = args[2];
            tesseractWorkDir = args[3];
            
            if (templatesZip.length() == 0 || 
                    templatesDir.length() == 0 || outputDir.length() == 0 || 
                    tesseractWorkDir.length() == 0) {
                printUsage();
            }
        } catch (Exception e) {
            printUsage();
    	}

        OCRBenchmark ocrBench = new OCRBenchmark();
        ocrBench.runFullTest(templatesZip, templatesDir, outputDir, tesseractWorkDir);
    }

    private static void printUsage() {
        System.out.println("Usage: java org.freeeed.ocr.benchmark.OCRBenchmark " +
            "<text templates zip> <text templates dir> <text templates " +
            "images output dir> <tesseract output dir>");	
    }
    
    private static final class OCRResult {

        private String file;
        private long time;
        private double match;

        public OCRResult(String file, long time, double match) {
            this.file = file;
            this.time = time;
            this.match = match;
        }
    }
}
