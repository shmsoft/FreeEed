package org.freeeed.stage;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FreeEed {

	private Options options;

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
			options = new Options();
			options.addOption("h", false, "Print help for FreeEed");
			options.addOption("stage", false, "Perform staging");
			options.addOption("par", true, "File with parameters for processing");
			options.addOption("in", true, "Input directory to stage");

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
