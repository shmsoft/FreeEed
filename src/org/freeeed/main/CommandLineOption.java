package org.freeeed.main;

/**
 * Processing of command-line options
 */
public enum CommandLineOption {
	HELP		("help", false, "print this help"), 
	PARAM_FILE	("param_file", true, "parameter file"), 
	DRY		("dry", false, "dry run - only read and echo parameters, but do no processing"),
        GUI             ("gui", false, "start graphical user interface"),
        ENRON		("enron", false, "process the enron data set (specific test script)"),
	VERSION		("version", false, "print the version of the software");
	private String name;
	private String help;
	private boolean hasArg;
	CommandLineOption(String name, boolean hasArg, String help) {
		this.name = name;
		this.hasArg = hasArg;
		this.help = help;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the help
	 */
	public String getHelp() {
		return help;
	}

	/**
	 * @return the hasArg
	 */
	public boolean isHasArg() {
		return hasArg;
	}
	@Override
	public String toString() {
		return name;
	}
}