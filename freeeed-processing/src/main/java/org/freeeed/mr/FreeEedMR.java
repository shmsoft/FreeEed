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
import org.freeeed.extractor.PstExtractor;
import org.freeeed.extractor.ZipFileExtractor;
import org.freeeed.main.*;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Project;
import org.freeeed.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FreeEedMR {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedMR.class);
    static Project project = Project.getCurrentProject();
    static File stagingFolder = new File(project.getStagingDir());
    private static volatile FreeEedMR mInstance;
    private long totalSize = 0;
    private int zipFileToExtract = 0, pstFileToExtract = 0;

    private FreeEedMR() {
    }

    public static FreeEedMR getInstance() {
        if (mInstance == null) {
            synchronized (FreeEedMR.class) {
                if (mInstance == null) {
                    mInstance = new FreeEedMR();
                }
            }
        }
        return mInstance;
    }

    public void run() {
        ProcessingStats.getInstance().setJobStarted(project.getProjectName());
        MetadataWriter metadataWriter = MetadataWriter.getInstance();
        try {
            metadataWriter.setup();
        } catch (IOException e) {
            e.printStackTrace();
        }

        decideNextJob();
    }

    public void reduceZipFile() {
        ProcessingStats.getInstance().addzipFilExtracted();
        zipFileToExtract--;
        if (zipFileToExtract == 0) {
            decideNextJob();
        }
    }

    private void processStageZipFiles() {
        List<File> files = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY);
        zipFileToExtract = files.size();
        files.forEach(temp -> {
            ExecutorPool.getInstance().getExecutorService().execute(new ZipFileExtractor(project, temp));
        });
    }

    public void reducePSTFile() {
        ProcessingStats.getInstance().addpstFilExtracted();
        pstFileToExtract--;
        if (pstFileToExtract == 0) {
            decideNextJob();
        }
    }

    private void processStagePSTFile() {
        List<File> files = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*pst)"), DirectoryFileFilter.DIRECTORY);
        pstFileToExtract = files.size();
        files.forEach(temp -> {
            ExecutorPool.getInstance().getExecutorService().execute(new PstExtractor(project, temp));
        });
    }

    /**
     * Decide what to do first
     */
    private void decideNextJob() {
        int filesZip = FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY).size();
        int filesPst = FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*pst)"), DirectoryFileFilter.DIRECTORY).size();
        System.out.println("Deciding");
        if (filesZip > 0) {
            ProcessingStats.getInstance().taskIsZip();
            processStageZipFiles();
        } else if (filesPst > 0) {
            ProcessingStats.getInstance().taskIsPST();
            processStagePSTFile();
        } else {
            ProcessingStats.getInstance().taskIsTika();
            mainProcess();
        }
    }

    private void mainProcess() {
        LOGGER.info("Starting Main Process");
        List<File> files = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*?)"), DirectoryFileFilter.DIRECTORY);
        ProcessingStats.getInstance().setTotalItem(files.size());
        files.forEach(temp -> {
            totalSize += temp.length();
        });
        ProcessingStats.getInstance().setTotalSize(totalSize);
        files.forEach(temp -> {
            DiscoveryFile discoveryFile = new DiscoveryFile(temp.toString(), temp.getName(), false);
            discoveryFile.setCustodian("Need custodian!");
            Runnable fileProcessor = null;
            if (EmlFileProcessor.isEml(discoveryFile)) {
                fileProcessor = new EmlFileProcessor(discoveryFile);
            } else if (Util.isSystemFile(discoveryFile)) {
                fileProcessor = new SystemFileProcessor(discoveryFile);
            } else {
                fileProcessor = new FileProcessor(discoveryFile);
            }
            ExecutorPool.getInstance().getExecutorService().execute(fileProcessor);
        });
    }

}
