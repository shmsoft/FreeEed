package org.freeeed.main;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FreeEed {

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
		FreeEed instance = new FreeEed(args);
		instance.processOptions(args);
	}

	public FreeEed(String[] args) {
	}

	private void processOptions(String[] args) {
		try {
			BasicParser parser = new BasicParser();
			CommandLine cl = parser.parse(options, args);

			if (cl.hasOption(FreeEedOption.HELP.getName())
					|| cl.getOptions().length == 0) {
				HelpFormatter f = new HelpFormatter();
				f.printHelp("java -jar FreeEed.jar [options]\n\n"
						+ "where options include:", options);
			} else if (cl.hasOption(FreeEedOption.VERSION.getName())) {
				System.out.println(FreeEedOption.getVersion());
			} else if (cl.hasOption(FreeEedOption.SEARCH.getName())) {
				openBrowserForSearch();
			} else if (cl.hasOption(FreeEedOption.DOC.getName())) {
				openBrowserGitHub();
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
}
