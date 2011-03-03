package org.freeeed.main;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FreeEed {

	private Options options = formOptions();

	private Options formOptions() {
		Options buildOptions = new Options();
		for (FreeEedOption o: FreeEedOption.values()) {
			buildOptions.addOption(o.getName(), o.isHasArg(), o.getHelp());
		}
		return buildOptions;
	}
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		FreeEed instance = new FreeEed(args);
	}

	public FreeEed(String[] args) {
		processOptions(args);
	}

	private void processOptions(String[] args) {
		try {
			BasicParser parser = new BasicParser();
			CommandLine cl = parser.parse(options, args);

			if (cl.hasOption('h') || cl.getOptions().length == 0) {
				HelpFormatter f = new HelpFormatter();
				f.printHelp("How to use FreeEed", options);
			} else {
				String [] in = cl.getOptionValues("in");	
				for (String i: in) {
					System.out.println(i);
				}
			}
		} catch (ParseException e) {
			// TODO use logging
			e.printStackTrace(System.out);

		}
	}
}
