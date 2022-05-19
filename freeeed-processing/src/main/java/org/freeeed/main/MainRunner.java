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
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class MainRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainRunner.class);

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
                LOGGER.error("metadataWriter error", e);
            }
            if (project.getProcessingEngine().equalsIgnoreCase("Piranha")) {
                // Start Piranha
                startPiranha();
                UtilUI.openBrowser(null, project.getSparkMonitoringURL());
            } else if (project.getProcessingEngine().equalsIgnoreCase("Standard")) {
                List<String> zipFiles = Files.readLines(
                        new File(project.getInventoryFileName()),
                        Charset.defaultCharset());
                for (String zipFileInput : zipFiles) {
                    String zipFile = zipFileInput.split(",")[0];
                    String custodian = zipFileInput.split(",")[1];
                    LOGGER.trace("Processing: " + zipFile);
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
                LOGGER.error("Non-existent processing engine");
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error in processing", e);
        }
    }
    private static void startPiranha() throws IOException {
        String command = "/home/mark/apps/spark/bin/spark-submit " +
                "--master spark://mark-workstation:7077 " +
                "--class x.ProcessFiles " +
                "/home/mark/projects/scaia/piranha-spark/target/scala-2.12/piranha_2.12-1.0.jar " +
                "/home/mark/projects/SHMsoft/FreeEed/freeeed-processing/output/freeeed-output/2/output/staging/flatinventory.csv 2> logs";
        OsUtil.runCommand(command);
    }
}
