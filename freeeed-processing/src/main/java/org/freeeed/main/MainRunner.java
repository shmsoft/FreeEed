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
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.ui.UtilUI;
import org.freeeed.util.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class MainRunner {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(MainRunner.class.getName());


    public static void run(String[] args) {
        try {
            Project project = Project.getCurrentProject();

            LuceneIndex luceneIndex = new LuceneIndex(
                    Settings.getSettings().getLuceneIndexDir(), project.getProjectCode(), null);

            SolrIndex.getInstance().init();
            //OfficePrint.getInstance().init();
            MetadataWriter metadataWriter = new MetadataWriter();
            try {
                metadataWriter.setup();
            } catch (IOException e) {
                LOGGER.severe("metadataWriter error");
            }
            if (project.getProcessingEngine().equalsIgnoreCase("Piranha")) {
                // Start Piranha
                PiranhaProcessor.startPiranha();
                UtilUI.openBrowser(null, project.getSparkMonitoringURL());
            } else if (project.getProcessingEngine().equalsIgnoreCase("Standard")) {
                List<String> zipFiles = Files.readLines(
                        new File(project.getInventoryFileName()),
                        Charset.defaultCharset());
                for (String zipFileInput : zipFiles) {
                    String zipFile = zipFileInput.split(",")[0];
                    String custodian = zipFileInput.split(",")[1];
                    LOGGER.fine("Processing: " + zipFile);
                    project.setCurrentCustodian(custodian);
                    // process archive file
                    ZipFileProcessor processor = new ZipFileProcessor(zipFile, metadataWriter, luceneIndex);
                    processor.process(false, null);
                }
                metadataWriter.cleanup();
                luceneIndex.destroy();
                SolrIndex.getInstance().flushBatchData();
                SolrIndex.getInstance().destroy();
                LOGGER.info("Processing finished");
            } else {
                LOGGER.severe("Non-existent processing engine");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.severe("Error in processing");
        }
    }
}
