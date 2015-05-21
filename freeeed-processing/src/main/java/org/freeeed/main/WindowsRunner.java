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

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.freeeed.data.index.LuceneIndex;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowsRunner {

    private static final Logger logger = LoggerFactory.getLogger(WindowsRunner.class);

    public static void run(String[] args) {
        try {
            Project project = Project.getProject();
            
            LuceneIndex luceneIndex = new LuceneIndex(
                    Settings.getSettings().getLuceneIndexDir(), project.getProjectCode(), null);
            luceneIndex.init();
            
            SolrIndex.getInstance().init();
            //OfficePrint.getInstance().init();
            
            List<String> zipFiles = Files.readLines(
                    new File(project.getInventoryFileName()),
                    Charset.defaultCharset());
            for (String zipFile : zipFiles) {
                logger.trace("Processing: " + zipFile);

                // process archive file
                ZipFileProcessor processor = new ZipFileProcessor(zipFile, null, luceneIndex);
                processor.process(false, null);
            }
            
            luceneIndex.destroy();
            
            SolrIndex.getInstance().flushBatchData();
            SolrIndex.getInstance().destroy();
            
            if (Project.getProject().isCreatePDF()) {
                OfficePrint.getInstance().destroy();
            }
            
            WindowsReduce.getInstance().cleanup(null);
            logger.info("Processing finished");
        } catch (IOException | InterruptedException e) {
            logger.error("Error in processing", e);
        }
    }
}
