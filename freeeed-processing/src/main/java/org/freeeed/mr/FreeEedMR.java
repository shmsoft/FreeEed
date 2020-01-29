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
package org.freeeed.mr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.freeeed.data.index.ESIndex;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.extractor.PstExtractor;
import org.freeeed.extractor.ZipFileExtractor;
import org.freeeed.main.DiscoveryFile;
import org.freeeed.main.EmlFileProcessor;
import org.freeeed.main.FileProcessor;
import org.freeeed.main.SystemFileProcessor;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FreeEedMR {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedMR.class);
    static Project project = Project.getCurrentProject();
    private static long totalSize = 0;
    private static int fileZips = 0;
    private static int pstFiles = 0;

    private static int fileToProcess = 0;

    static File result = new File(project.getStagingDir());
    private static int numberOfThreads = 6;

    private static ArrayList<String> fileList = new ArrayList<>();


    public static void main() {
        processStageZipFiles();
    }

    public static synchronized void reduceZipFile() {
        fileZips--;
        if (fileZips == 0) {
            LOGGER.info("ZIP Done");
            processStagePSTFile();
        }
    }

    public static synchronized void reducePSTFile() {
        pstFiles--;
        if (pstFiles == 0) {
            LOGGER.info("PST Done");
            mainProcess();
        }
    }

    static synchronized void reduceFileToProcess(String file) {
        fileToProcess--;
        System.out.println(fileToProcess);

        fileList.remove(file);

        if (fileToProcess < 15) {
            System.out.println(fileList);

        }


    }

    private static void processStageZipFiles() {
        Collection files = FileUtils.listFiles(result, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY);
        if (!files.isEmpty()) {
            fileZips = files.size();
            files.forEach(temp -> {
                ZipFileExtractor zipFileExtractor = new ZipFileExtractor(project, (File) temp);
                zipFileExtractor.extract();
            });
        } else {
            processStagePSTFile();
        }
    }

    private static void processStagePSTFile() {
        Collection files = FileUtils.listFiles(result, new RegexFileFilter("^(.*pst)"), DirectoryFileFilter.DIRECTORY);
        if (!files.isEmpty()) {
            pstFiles = files.size();
            files.forEach(temp -> {
                PstExtractor pstExtractor = new PstExtractor(project, (File) temp);
                pstExtractor.extract();
            });
        } else {
            mainProcess();
        }
    }

    private static void mainProcess() {
        LOGGER.info("Starting Main Process");
        ProcessingStats.getInstance().setJobStarted(project.getProjectName());
        MetadataWriter metadataWriter = MetadataWriter.getInstance();
        try {
            metadataWriter.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LuceneIndex luceneIndex = new LuceneIndex(Settings.getSettings().getLuceneIndexDir(), project.getProjectCode(), null);
        luceneIndex.init();
        ESIndex.getInstance().init();
        Collection files = FileUtils.listFiles(result, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        ProcessingStats.getInstance().setTotalItem(files.size());
        ExecutorService executorService = Executors.newFixedThreadPool(6);
        fileToProcess = files.size();

        files.forEach(temp -> {
            File f = (File) temp;
            fileList.add(f.getName());

        });


        files.forEach(temp -> {
            File f = (File) temp;
            totalSize += f.length();
            DiscoveryFile discoveryFile = new DiscoveryFile(temp.toString(), f.getName(), false);
            discoveryFile.setCustodian("Need custodian!");
            Runnable fileProcessor = null;
            if (EmlFileProcessor.isEml(discoveryFile)) {
                fileProcessor = new EmlFileProcessor(metadataWriter, luceneIndex, discoveryFile);
            } else if (Util.isSystemFile(discoveryFile)) {
                fileProcessor = new SystemFileProcessor(metadataWriter, luceneIndex, discoveryFile);
            } else {
                fileProcessor = new FileProcessor(metadataWriter, luceneIndex, discoveryFile);
            }
            if (fileProcessor != null) {
                executorService.execute(fileProcessor);
            }


        });
        ProcessingStats.getInstance().setTotalSize(totalSize);
        ESIndex.getInstance().destroy();
    }


}
