package org.freeeed.main;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FreeEedMain {
	
	private Options options = formOptions();
	private FreeEedParam param = new FreeEedParam();
	
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
		FreeEedMain instance = new FreeEedMain(args);
		instance.processOptions(args);
	}
	
	public FreeEedMain(String[] args) {
	}
	
	private void processOptions(String[] args) {
		try {
			BasicParser parser = new BasicParser();
			CommandLine cl = parser.parse(options, args);

			// one-time actions
			if (cl.hasOption(FreeEedOption.HELP.getName())
					|| cl.getOptions().length == 0) {
				HelpFormatter f = new HelpFormatter();
				f.printHelp("java -jar FreeEed.jar [options]\n\n"
						+ "where options include:", options);
			} else if (cl.hasOption(FreeEedOption.VERSION.getName())) {
				System.out.println(FreeEedOption.getVersion());
			} else if (cl.hasOption(FreeEedOption.SEARCH.getName())) {
				openBrowserForSearch();
				System.exit(0);
			} else if (cl.hasOption(FreeEedOption.DOC.getName())) {
				openBrowserGitHub();
				System.exit(0);
			}
			// independent actions
			if (cl.hasOption(FreeEedOption.PARAM_FILE.getName())) {
				processParamFile(cl.getOptionValue(FreeEedOption.PARAM_FILE.getName()));
			}			
			if (cl.hasOption(FreeEedOption.INPUT.getName())) {
				processInputOption(cl.getOptionValues(FreeEedOption.INPUT.getName()));
			}
		} catch (ParseException e) {
			// TODO use logging
			e.printStackTrace(System.out);
			
		}
	}
	
	private void openBrowserForSearch() {
		try {
			Desktop desktop = Desktop.getDesktop();
			File currentDir = new File(".");
			String uriStr = "file:///" + currentDir.getAbsolutePath() + "/search.html";
			URI uri = new URI(uriStr);
			desktop.browse(uri);
		} catch (Exception e) {
			System.out.println("Oops! Something did not work :(");
			e.printStackTrace(System.out);
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
	
	private void processInputOption(String[] dirs) {
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
	}

	private void processParamFile(String fileName) {
		try {
			if (!new File(fileName).exists()) {
				throw new IOException("File does not exist");
			}
			param.parseParameters(fileName);
		} catch (Exception e) {
			System.out.println("Error in parameter file: " + fileName);
			e.printStackTrace(System.out);
			// follow the "fail-fast" design pattern
			System.exit(1);
		}
		
	}
}
