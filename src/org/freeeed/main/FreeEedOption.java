package org.freeeed.main;

public enum FreeEedOption {
	HELP	("help", false, "print help (for complete documentation go to "
			+ "https://github.com/markkerzner/FreeEed"), 
	INPUT	("input", true, "input directory"), 
	PAR		("par", true, "parameter file"), 
	HADOOP	("hadoop", false, "run processing on hadoop cluster "
			+ "(without this option, run processing locally)");
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
}
