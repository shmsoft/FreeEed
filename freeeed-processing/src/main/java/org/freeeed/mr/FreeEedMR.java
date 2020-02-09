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
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FreeEedMR {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedMR.class);
    static Project project = Project.getCurrentProject();
    static File result = new File(project.getStagingDir());
    private static long totalSize = 0;
    private static ExecutorService executorService = Executors.newFixedThreadPool(6);
    private static int zipFileToExtract = 0;

    public void run() {
        ProcessingStats.getInstance().setJobStarted(project.getProjectName());
        decideNextJob();
    }

    public static synchronized void reduceZipFile() {
        ProcessingStats.getInstance().addzipFilExtracted();
        zipFileToExtract--;
        System.out.println(zipFileToExtract);
        if (zipFileToExtract == 0) {
            decideNextJob();
        }
    }

    /**
     * This step is IO heavy and should not be multi threaded
     */
    private static void processStageZipFiles() {
        List<File> files = (List<File>) FileUtils.listFiles(result, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY);
        zipFileToExtract = files.size();
        files.forEach(temp -> {
            executorService.execute(new ZipFileExtractor(project, temp));
        });
    }

    public static synchronized void reducePSTFile() {
        ProcessingStats.getInstance().addpstFilExtracted();
        if (ProcessingStats.getInstance().getPstFileExtracted() == ProcessingStats.getInstance().getTotalPstFileToExtract()) {
            decideNextJob();
        }
    }

    private static void processStagePSTFile() {
        List<File> files = (List<File>) FileUtils.listFiles(result, new RegexFileFilter("^(.*pst)"), DirectoryFileFilter.DIRECTORY);
        //ProcessingStats.getInstance().setTotalPstFileToExtract(files.size() - ProcessingStats.getInstance().getTotalPstFileToExtract());

        files.forEach(temp -> {
            PstExtractor extractor = new PstExtractor(project, temp);
            executorService.execute(extractor);
        });
    }

    /**
     * Decide what to do first
     */
    private static void decideNextJob() {
        int filesZip = FileUtils.listFiles(result, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY).size();
        int filesPst = FileUtils.listFiles(result, new RegexFileFilter("^(.*pst)"), DirectoryFileFilter.DIRECTORY).size();
        System.out.println("Deciding");
        if (filesZip > 0) {
            ProcessingStats.getInstance().taskIsZip();
            processStageZipFiles();
        } else if (filesPst > 0) {
            processStagePSTFile();
        } else {
            executorService.shutdownNow();
            mainProcess();
        }
    }

    private static void mainProcess() {
        ExecutorService executorService = Executors.newFixedThreadPool(6);
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
