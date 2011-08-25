package org.freeeed.main;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.Configuration;
import org.freeeed.services.Stats;
import org.freeeed.ui.FreeEedUI;

/**
 * Main application instance
 *
 * @author mark
 */
public class FreeEedMain {

    private static FreeEedMain instance = new FreeEedMain();
    private CommandLine commandLine;
    private Configuration processingParameters;

    public static FreeEedMain getInstance() {
        return instance;
    }

    public Configuration getProcessingParameters() {
        return processingParameters;
    }
    private Options options = formOptions();

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
        String customParameterFile = null;
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
                System.out.println(Version.getVersion());
            } else if (commandLine.hasOption(CommandLineOption.GUI.getName())) {
                openGUI();
            } else {
                if (commandLine.hasOption(CommandLineOption.PARAM_FILE.getName())) {
                    // independent actions
                    customParameterFile = commandLine.getOptionValue(CommandLineOption.PARAM_FILE.getName());
                    setProcessingParameters(ParameterProcessing.collectProcessingParameters(customParameterFile));
                    ParameterProcessing.echoProcessingParameters(getProcessingParameters());
                }
                if (commandLine.hasOption(CommandLineOption.DRY.getName())) {
                    System.out.println("Dry run - exiting now.");
                } else {
                    if (FreeEedMain.getInstance().getProcessingParameters().getString("stage") != null) {
                        runStagePackageInput();
                    }
                    String runWhere = FreeEedMain.getInstance().getProcessingParameters().getString(ParameterProcessing.PROCESS_WHERE);
                    if (runWhere != null) {
                        runProcessing(runWhere);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Search selected inputs via Hadoop
     *
     * @param runWhere determines whether processing occurs on local, private, or EC2 Hadoop instance
     * @throws FreeEedException
     */
    public void runProcessing(String runWhere) throws FreeEedException {
        Stats.getInstance().setJobStarted();
        new Thread(new ActionProcessing(runWhere)).start();
    }

    /**
     * Take selected inputs and package into zip files which are distributed
     * for processing amongst each Hadoop processing instance
     *
     * @throws Exception
     */
    public void runStagePackageInput() throws Exception {
        // TODO - think through the use of threads, locking, communication, cancel, etc.
        new Thread(new ActionStaging()).start();
    }

    private void openGUI() {
        FreeEedUI.main(null);
    }

    /**
     * @param processingParameters the processingParameters to set
     */
    public void setProcessingParameters(Configuration processingParameters) {
        this.processingParameters = processingParameters;
    }
}
