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
import org.freeeed.data.index.ESIndex;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class WindowsRunner {

    private static final Logger logger = LoggerFactory.getLogger(WindowsRunner.class);

    public static void run(String[] args) {
        try {
            Project project = Project.getCurrentProject();

            LuceneIndex luceneIndex = new LuceneIndex(
                    Settings.getSettings().getLuceneIndexDir(), project.getProjectCode(), null);
            luceneIndex.init();

            ESIndex.getInstance().init();

            List<String> zipFiles = Files.readLines(
                    new File(project.getInventoryFileName()),
                    Charset.defaultCharset());
            for (String zipFile : zipFiles) {
                logger.trace("Processing: " + zipFile);

                MetadataWriter metadataWriter = new MetadataWriter();
                metadataWriter.setup();
                // process archive file
                ZipFileProcessor processor = new ZipFileProcessor(zipFile, metadataWriter, luceneIndex);
                processor.process(false, null);
            }

            ESIndex.getInstance().destroy();

            logger.info("Processing finished");
        } catch (IOException | InterruptedException e) {
            logger.error("Error in processing", e);
        }
    }
}
