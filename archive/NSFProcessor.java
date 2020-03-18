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

import java.io.File;
import java.io.IOException;

import org.freeeed.data.index.LuceneIndex;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;



import org.freeeed.mr.MetadataWriter;

/**
 * 
 * Class NSFProcessor.
 * 
 * @author ilazarov.
 *
 */
public class NSFProcessor {

    private String nsfFilePath;
    private MetadataWriter metadataWriter;
    private LuceneIndex luceneIndex;

    public NSFProcessor(String nsfFilePath, MetadataWriter metadataWriter, LuceneIndex luceneIndex) {
        this.nsfFilePath = nsfFilePath;
        this.metadataWriter = metadataWriter;
        this.luceneIndex = luceneIndex;
    }
    
    public static boolean isNSF(String fileName) {
        return "nsf".equalsIgnoreCase(Util.getExtension(fileName));
    }
    
    public void process() throws IOException, Exception {
        String outputDir = Settings.getSettings().getNFSDir();
        File nsfDirFile = new File(outputDir);
        if (nsfDirFile.exists()) {
            Util.deleteDirectory(nsfDirFile);
        }
        
        extractEmails(nsfFilePath, outputDir);
        collectEmails(outputDir);
    }
    
    private void extractEmails(String nsfPath, String outputDir) {
        NSFParser parser = new NSFParser();
        parser.parseNSF(nsfPath, outputDir, metadataWriter);
    }
    
    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            ZipFileProcessor fileProcessor = new ZipFileProcessor(emailDir, metadataWriter, luceneIndex);
            fileProcessor.process(false, null);
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file : files) {
                collectEmails(file.getPath());
            }
        }
    }
   
}
