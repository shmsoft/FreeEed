package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.configuration.Configuration;
import org.freeeed.ui.FreeEedUI;

public class FreeEedMain {

	private static FreeEedMain instance = new FreeEedMain();
	private CommandLine commandLine;
	private Configuration processingParameters;

	public String getVersion() {
		return "FreeEed V2.0.1";
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
				System.out.println(getVersion());
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

	public void runProcessing(String runWhere) {
		System.out.println("Processing: " + runWhere);
		if ("local".equals(runWhere)) {
			try {
				String[] processingArguments = new String[1];
				processingArguments[0] = getProcessingParameters().getString("output-dir");
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

	public void stagePackageInput() throws IOException {
		// TODO better setting of dirs?
		String stagingDir = PackageArchive.stagingDir;
		LinuxUtil.runLinuxCommand("rm -fr " + stagingDir);
		new File(stagingDir).mkdirs();
		
		String[] dirs = processingParameters.getStringArray(ParameterProcessing.PROJECT_INPUTS);
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
			System.exit(0);
		}
		PackageArchive.writeInventory();
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
