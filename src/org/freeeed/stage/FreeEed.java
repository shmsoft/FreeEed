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
      options.addOption("h", false, "Print help for this application");
      options.addOption("u", true, "The username to use");
      options.addOption("dsn", true, "The data source to use");

      BasicParser parser = new BasicParser();
      CommandLine cl = parser.parse(options, args);

      if (cl.hasOption('h')) {
        HelpFormatter f = new HelpFormatter();
        f.printHelp("OptionsTip", options);
      } else {
        System.out.println(cl.getOptionValue("u"));
        System.out.println(cl.getOptionValue("dsn"));
      }
    } catch (ParseException e) {
      // TODO use logging
      e.printStackTrace(System.out);

    }
  }
}
