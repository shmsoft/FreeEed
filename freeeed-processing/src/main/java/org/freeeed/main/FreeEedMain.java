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

import org.freeeed.util.PlatformUtil;
import java.io.File;
import java.text.DecimalFormat;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;
import org.freeeed.services.Util;
import org.freeeed.ui.ProcessProgressUI;
import org.freeeed.ui.StagingProgressUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application instance
 *
 * @author mark
 */
public class FreeEedMain {

    Logger logger = LoggerFactory.getLogger(FreeEedMain.class);
    private static final FreeEedMain instance = new FreeEedMain();
    private CommandLine commandLine;
    //private Configuration processingParameters;    

    public static FreeEedMain getInstance() {
        return instance;
    }
    private final Options options = formOptions();

    private Options formOptions() {
        Options buildOptions = new Options();
        for (CommandLineOption o : CommandLineOption.values()) {
            buildOptions.addOption(o.getName(), o.isHasArg(), o.getHelp());
        }
        return buildOptions;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        instance.processOptions(args);
    }

    private FreeEedMain() {
    }

    /**
     * Process the command line arguments
     *
     * @param args command line arguments
     */
    private void processOptions(String[] args) {
        String customParameterFile;
        Project project = null;
        try {
            BasicParser parser = new BasicParser();
            commandLine = parser.parse(options, args);

            // one-time actions
            if (commandLine.hasOption(CommandLineOption.HELP.getName())
                    || commandLine.getOptions().length == 0) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("java -jar FreeEed.jar [options]\n\n"
                        + "where options include:", options);
            } else if (commandLine.hasOption(CommandLineOption.VERSION.getName())) {
                System.out.println(Version.getVersionAndBuild());
            } else if (commandLine.hasOption(CommandLineOption.GUI.getName())) {
                openGUI();
            } else if (commandLine.hasOption(CommandLineOption.ENRON.getName())) {
                processEnronDataSet();
            } else {
                if (commandLine.hasOption(CommandLineOption.PARAM_FILE.getName())) {
                    // independent actions
                    customParameterFile = commandLine.getOptionValue(CommandLineOption.PARAM_FILE.getName());
                    project = Project.loadFromFile(new File(customParameterFile));
                }
                if (commandLine.hasOption(CommandLineOption.DRY.getName())) {
                    System.out.println("Dry run - exiting now.");
                } else {
                    if (project.isStage()) {
                        stagePackageInput();
                    }
                    String runWhere = project.getProcessWhere();
                    if (runWhere != null) {
                        process(runWhere);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in processing", e);
        }
    }

    /**
     * Process staged files
     *
     * @param runWhere determines whether processing occurs on local, private, or EC2 Hadoop instance
     * @throws FreeEedException
     */
    public void process(String runWhere) {
        String projectName = Project.getProject().getProjectName();
        Stats.getInstance().setJobStarted(projectName);
        try {
            new ActionProcessing(runWhere).process();
        } catch (Exception e) {
            // TODO - what to do in case of exception
            e.printStackTrace(System.out);
        }
    }

    /**
     * Process from GUI in a thread
     *
     * @param runWhere determines whether processing occurs on local, private, or EC2 Hadoop instance
     * @throws FreeEedException
     */
    public void runProcessing(String runWhere) {
        ProcessProgressUI ui = new ProcessProgressUI(null, true);
        ui.setVisible(true);        
    }

    /**
     * Stage (package) the input files
     *
     * @throws Exception
     */
    public void stagePackageInput() throws Exception {
        new ActionStaging().stagePackageInput();
    }

    /**
     * Stage from GUI in a thread
     *
     * @throws Exception
     */
    public void runStagePackageInput() throws Exception {
        StagingProgressUI ui = new StagingProgressUI(null, true);
        ui.setVisible(true);
    }

    // TODO main engine should not mention gui
    private void openGUI() {
        //SHMcloudUI.main(null);
    }

    private void processEnronDataSet() {
        int ENRON_SET_SIZE = 154;
        String localDir = "freeeed-output/";
        String output = "output/";
        String dir = "/mnt/tmp/";
        new File(dir + "results").mkdirs();
        for (int i = 1; i <= ENRON_SET_SIZE; ++i) {
            try {
                DecimalFormat decimalFormat = new DecimalFormat("enron000");
                String projectName = decimalFormat.format(i);
                if (new File(dir + projectName + ".project").exists() == false) {
                    continue;
                }
                String outputPath = dir + "results/" + projectName + "/";
                if (new File(outputPath).exists()) {
                    continue;
                }
                File localDirFile = new File(localDir);
                if (localDirFile.exists()) {
                    Util.deleteDirectory(localDirFile);
                }
                String[] argv = new String[2];
                argv[0] = "-param_file";
                argv[1] = dir + projectName + ".project";
                processOptions(argv);
                // copy to local output dir
                new File(outputPath).mkdirs();
                String command = "cp " + localDir + output
                        + "native.zip " + outputPath + projectName + ".zip";
                PlatformUtil.runCommand(command);
                command = "cp " + localDir + output + "part-r-00000 "
                        + outputPath + projectName + ParameterProcessing.METADATA_FILE_EXT;
                PlatformUtil.runCommand(command);
                command = "mv logs/stats.txt "
                        + outputPath + projectName + ".txt";
                PlatformUtil.runCommand(command);
                // place on amazon s3
                // like this, aws put freeeed.org/enron/results/enron001/enron001.zip enron001.zip
                command = "aws put freeeed.org/enron/results/"
                        + projectName + ".zip " + outputPath + projectName + ".zip";
                PlatformUtil.runCommand(command);
                command = "aws put freeeed.org/enron/results/"
                        + projectName + ".csv " + outputPath + projectName + ParameterProcessing.METADATA_FILE_EXT;
                PlatformUtil.runCommand(command);
                command = "aws put freeeed.org/enron/results/"
                        + projectName + ".txt " + outputPath + projectName + ".report.txt";
                PlatformUtil.runCommand(command);
            } catch (Exception e) {
                logger.error("Error, what is it? ", e);
            }
        }
    }
}
