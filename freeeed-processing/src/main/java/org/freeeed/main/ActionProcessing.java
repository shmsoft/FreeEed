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

import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.ui.ProcessProgressUI;
import org.freeeed.util.AutomaticUICaseCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that configures Hadoop and performs data search
 *
 * @author mark
 */
public class ActionProcessing implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ActionProcessing.class);
    private String runWhere;
    private ProcessProgressUI processUI = null;
    private boolean interrupted = false;
    /**
     * @param runWhere determines whether Hadoop runs on EC2, local cluster, or local machine
     */
    public ActionProcessing(String runWhere) {
        this.runWhere = runWhere;        
    }

    public ActionProcessing(ProcessProgressUI processUI) {
        this.processUI = processUI;
    }

    @Override
    public void run() {
        try {
            process();
        } catch (Exception e) {
            logger.error("Running action processing", e);
        }
    }

    /**
     * @throws Exception
     */
    public void process() throws Exception {
        Project project = Project.getCurrentProject();

        logger.info("Processing project: {}", project.getProjectName());

        System.out.println("Processing: " + runWhere);

        // this code only deals with local Hadoop processing
        if (project.isEnvLocal()) {
            try {
                // check output directory
                String[] processingArguments = new String[2];
                processingArguments[0] = project.getInventoryFileName();
                processingArguments[1] = project.getResultsDir();
                // check if output directory exists
                if (new File(processingArguments[1]).exists()) {
                    logger.error("Please remove output directory {}", processingArguments[0]);
                    logger.info("For example, in Unix you can do rm -fr {}", processingArguments[0]);
                    throw new RuntimeException("Output directory not empty");
                }
                FreeEedMR.main(processingArguments);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IllegalStateException(e.getMessage());
            }
        }
        logger.info("Processing done");
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.setDone();
        }

        if (project.isSendIndexToSolrEnabled()) {
            logger.info("Creating new case in FreeEed UI at: {}", Settings.getSettings().getReviewEndpoint());

            AutomaticUICaseCreator caseCreator = new AutomaticUICaseCreator();
            AutomaticUICaseCreator.CaseInfo info = caseCreator.createUICase();

            logger.info("Case created: {}", info.getCaseName());
        }
    }
    /**
     *
     * @param interrupted
     */
    public synchronized void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;        
    }
}
