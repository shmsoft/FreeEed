package org.freeeed.main;

/**
 * Processing of command-line options
 */
public enum FreeEedOption {
	HELP	("h", false, "print help (for complete documentation go to "
			+ "https://github.com/markkerzner/FreeEed"), 
	INPUT	("input", true, "input directory"), 
	PARAM_FILE		("param_file", true, "parameter file"), 
	HADOOP	("hadoop", false, "run processing on hadoop cluster "
			+ "(without this option, run processing locally)"),
	SEARCH	("search", false, "open the default browser to search the results"),
	DOC		("doc", false, "go to project documentation on GitHub"),
	VERSION	("version", false, "print the version of the software");
	private String name;
	private String help;
	private boolean hasArg;
	FreeEedOption(String name, boolean hasArg, String help) {
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
	public static String getVersion() {
		return "FreeEed V0.1.0";
	}
}
