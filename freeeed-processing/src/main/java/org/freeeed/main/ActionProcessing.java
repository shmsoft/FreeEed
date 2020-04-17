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

import org.freeeed.LoadeDiscovery.DatLoader;
import org.freeeed.data.index.ESIndex;
import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.Project;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Settings;
import org.freeeed.util.AutomaticUICaseCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Thread that configures Hadoop and performs data search
 *
 * @author mark
 */
public class ActionProcessing implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ActionProcessing.class);
    private FreeEedUIHelper ui;

    public ActionProcessing(FreeEedUIHelper ui) {
        this.ui = ui;
    }

    public void process() {
        Project project = Project.getCurrentProject();
        ProcessingStats.getInstance().setUi(ui);

        if (project.isSendIndexToESEnabled()) {
            logger.info("Creating new case in FreeEed UI at: {}", Settings.getSettings().getReviewEndpoint());
            AutomaticUICaseCreator caseCreator = new AutomaticUICaseCreator();
            AutomaticUICaseCreator.CaseInfo info = null;
            try {
                info = caseCreator.createUICase();
                logger.info("Case created: {}", info.getCaseName());
                ESIndex.getInstance().init();
            } catch (IOException e) {
                logger.info("Can not reach review services", e);
            }
        }

        logger.info("Processing project: {}", project.getProjectName());
        if (project.getDataSource() == Project.DATA_SOURCE_BLOCKCHAIN) {
            uploadJsonToES(project);
        } else if (project.getDataSource() == Project.DATA_SOURCE_QB) {
            processQBFile(project);
        } else if (project.getDataSource() == Project.DATA_SOURCE_LOAD_FILE) {

            DatLoader.getInstance().run();

        } else {
            FreeEedMR.getInstance().run();
        }

    }

    private void uploadJsonToES(Project project) {
        int totalSize = project.getBlockTo() - project.getBlockFrom();


        String filePath = project.getProjectFilePath();
        if (filePath == null || !new File(filePath).exists()) {
            return;
        }

        final String projectCode = project.getProjectCode();
        //final String indicesName = ESIndex.ES_INSTANCE_DIR + "_" + projectCode;

        //ESIndexUtil.createIndices(indicesName);

        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            AtomicInteger size = new AtomicInteger();
            stream.forEach(line -> {
                int pipeIndex = line.indexOf("|");
                int blockNumber = Integer.parseInt(line.substring(0, pipeIndex));
                line = line.substring(pipeIndex + 1);
                //   processProgressUIHelper.setProcessingState(line.substring(0, Math.min(15, line.length())) + "...");
                //ESIndexUtil.addBlockChainToES(line, indicesName, blockNumber);
                //  processProgressUIHelper.updateProgress(size.incrementAndGet());
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void processQBFile(Project project) {
        String[] inputs = project.getInputs();

        if (inputs == null || inputs.length == 0) {
            return;
        }

        for (String input : inputs) {
            File qbCSVFile = new File(input);
            int totalFiles = 1;
            if (qbCSVFile.isDirectory()) {
                totalFiles = qbCSVFile.listFiles().length;
            }

            final String projectCode = project.getProjectCode();
            //final String indicesName = ESIndex.ES_INSTANCE_DIR + "_" + projectCode;

            // ESIndexUtil.createIndices(indicesName);
            AtomicInteger progressCounter = new AtomicInteger();

            //read files in parallel and process sequentially
            if (qbCSVFile.isDirectory()) {
                Arrays.stream(qbCSVFile.listFiles()).parallel().forEach(file -> {
                    if (file.getName().toLowerCase().endsWith("csv")) {
                        // QBCsvParser.readCSVAsJson(file.getPath(), indicesName);
                    }
                    //processProgressUIHelper.setProcessingState(file.getName().substring(0, Math.min(15, file.getName().length())) + "...");
                    //processProgressUIHelper.updateProgress(progressCounter.incrementAndGet());
                });
            } else {
                if (qbCSVFile.getName().toLowerCase().endsWith("csv")) {
                    // QBCsvParser.readCSVAsJson(input, indicesName);
                }
            }
        }

    }

    public synchronized void setInterrupted() {
        boolean interrupted = true;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
