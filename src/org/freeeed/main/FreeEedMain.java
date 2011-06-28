package org.freeeed.main;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.freeeed.ui.FreeEedUI;

public class FreeEedMain {

    private static FreeEedMain instance = new FreeEedMain();
    private CommandLine commandLine;
    private static final String defaultParameterFile = "default.freeeed.properties";
    private Configuration processingParameters;
    private static final String logDir = "logs";

    public String getVersion() {
        return "FreeEed V2.0.0";
    }

    public static FreeEedMain getInstance() {
        return instance;
    }

    public Configuration getProcessingParameters() {
        return processingParameters;
    }
    private Options options = formOptions();

    private Options formOptions() {
        Options buildOptions = new Options();
        for (FreeEedOption o : FreeEedOption.values()) {
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

    private void processOptions(String[] args) {
        String customParameterFile = null;
        try {
            BasicParser parser = new BasicParser();
            commandLine = parser.parse(options, args);

            // one-time actions
            if (commandLine.hasOption(FreeEedOption.HELP.getName())
                    || commandLine.getOptions().length == 0) {
                HelpFormatter f = new HelpFormatter();
                f.printHelp("java -jar FreeEed.jar [options]\n\n"
                        + "where options include:", options);
            } else if (commandLine.hasOption(FreeEedOption.VERSION.getName())) {
                System.out.println(getVersion());
            } else if (commandLine.hasOption(FreeEedOption.GUI.getName())) {
                openGUI();
            } else {
                if (commandLine.hasOption(FreeEedOption.PARAM_FILE.getName())) {
                    // independent actions
                    customParameterFile = commandLine.getOptionValue(FreeEedOption.PARAM_FILE.getName());
                    processingParameters = collectProcessingParameters(customParameterFile);
                    echoProcessingParameters(processingParameters);
                }
                if (commandLine.hasOption(FreeEedOption.DRY.getName())) {
                    System.out.println("Dry run - exiting now.");
                } else {
		    if (FreeEedMain.getInstance().getProcessingParameters().getString("stage") != null) {                    
                        stagePackageInput();
                    }
		    String runWhere = FreeEedMain.getInstance().getProcessingParameters().getString("process");
                    if (runWhere != null) {
                        runProcessing(runWhere);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void runProcessing(String runWhere) {
        System.out.println("Processing: " + runWhere);
        if ("local".equals(runWhere)) {
            try {
                String[] processingArguments = new String[1];
                processingArguments[0] = processingParameters.getString("output.dir");
                if (new File(processingArguments[0]).exists()) {
                    System.out.println("Please remove output directory " + processingArguments[0]);
                    System.out.println("For example, in Linux you can do rm -fr " + processingArguments[0]);
                    return;
                }
                FreeEedProcess.main(processingArguments);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    private void openBrowserGitHub() {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("https://github.com/markkerzner/FreeEed");
            desktop.browse(uri);
        } catch (Exception e) {
            System.out.println("Oops! Something did not work :(");
            e.printStackTrace(System.out);
        }
    }

    private void stagePackageInput() throws IOException {
        // TODO better setting of dirs?
        String stagingDir = PackageArchive.stagingDir;
        LinuxUtil.runLinuxCommand("rm -fr " + stagingDir); 
        new File(stagingDir).mkdirs();

        String[] dirs = processingParameters.getStringArray("input");
        System.out.println("Packaging (staging) the following directories for processing:");
        PackageArchive packageArchive = new PackageArchive();
        // TODO - set custom packaging parameters		
        try {

            for (String dir : dirs) {
                System.out.println(dir);
                packageArchive.packageArchive(dir);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // follow the "fail-fast" design pattern
            System.exit(1);
        }
        PackageArchive.writeInventory();
    }

    private Configuration collectProcessingParameters(String customParametersFile) {
        CompositeConfiguration cc = new CompositeConfiguration();
        try {
            // custom parameter file is first priority
            if (customParametersFile != null) {
                Configuration customProperties = new PropertiesConfiguration(customParametersFile);
                cc.addConfiguration(customProperties);
            }
            // default parameter file is last priority
            Configuration defaults = new PropertiesConfiguration(defaultParameterFile);
            cc.addConfiguration(defaults);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // follow the "fail-fast" design pattern
            System.exit(1);
        }
        return cc;
    }

//    private Configuration getCommandLineProperties() {
//        Configuration commandLineConfig = new PropertiesConfiguration();
//        if (commandLine.hasOption(FreeEedOption.CULL.getName())) {
//            String value = commandLine.getOptionValue(FreeEedOption.CULL.getName());
//            commandLineConfig.setProperty("cull", value);
//        }
//        return commandLineConfig;
//    }

    private void echoProcessingParameters(Configuration configuration)
            throws ConfigurationException, MalformedURLException {
        SimpleDateFormat fileNameFormat = new SimpleDateFormat(
                "yyMMdd_HHmmss");
        String runParameterFileName = "freeeed.parameters."
                + fileNameFormat.format(new Date()) + ".properties";
        PropertiesConfiguration configToSave = new PropertiesConfiguration();
        configToSave.append(configuration);
        new File(logDir).mkdirs();
        String paramPath = logDir + "/" + runParameterFileName;
        configToSave.save(paramPath);
        System.out.println("Processing parameters were saved to " + paramPath);
    }
    private void openGUI() {
        FreeEedUI.main(null);
    }
}
