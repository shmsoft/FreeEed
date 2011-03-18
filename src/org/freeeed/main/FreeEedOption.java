package org.freeeed.main;

/**
 * Processing of command-line options
 */
public enum FreeEedOption {
	HELP	("h", false, "print this help"), 
	INPUT	("input", true, "input directory(s)"), 
	PARAM_FILE		("param_file", true, "parameter file"), 
	PROCESS	("process", true, "run processing (possible options are "
			+ "local, hadoop, and ec2)"),
	INDEX	("index", false, "create index for searches"),
	DB		("db", false, "store results in a database"),
	SEARCH	("search", false, "open the default browser to search the results"),
	DOC		("doc", false, "go to project documentation on GitHub"),
	CULL	("cull", true, "cull on given string(s)"),
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
	}}
