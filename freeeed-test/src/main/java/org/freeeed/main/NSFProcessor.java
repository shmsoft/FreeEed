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
package org.freeeed.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.lotus.NSFParser;
import org.freeeed.services.FreeEedUtil;


import com.google.common.io.Files;

/**
 * 
 * Class NSFProcessor.
 * 
 * @author ilazarov.
 *
 */
public class NSFProcessor implements ActionListener {

    private String nsfFilePath;
    private Context context;
    private LuceneIndex luceneIndex;

    public NSFProcessor(String nsfFilePath, Context context, LuceneIndex luceneIndex) {
        this.nsfFilePath = nsfFilePath;
        this.context = context;
        this.luceneIndex = luceneIndex;
    }
    
    public static boolean isNSF(String fileName) {
        if ("nsf".equalsIgnoreCase(FreeEedUtil.getExtension(fileName))) {
            return true;
        }
        return false;
    }
    
    public void process() throws IOException, Exception {
        String outputDir = ParameterProcessing.NSF_OUTPUT_DIR;
        File nsfDirFile = new File(outputDir);
        if (nsfDirFile.exists()) {
            Files.deleteRecursively(nsfDirFile);
        }
        
        extractEmails(nsfFilePath, outputDir);
        collectEmails(outputDir);
    }
    
    private void extractEmails(String nsfPath, String outputDir) {
        NSFParser parser = new NSFParser();
        parser.parseNSF(nsfPath, outputDir, context);
    }
    
    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            ZipFileProcessor fileProcessor = new ZipFileProcessor(emailDir, context, luceneIndex);
            fileProcessor.process();
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file : files) {
                collectEmails(file.getPath());
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        // inform Hadoop that we are alive
        if (context != null) {
            context.progress();
        }
    }
}
