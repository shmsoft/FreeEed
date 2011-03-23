package org.freeeed.main;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Processing parameters
 */
public class FreeEedParam extends PropertiesConfiguration {
	private int filesPerArchive = 100;

	/**
	 * @return the filesPerArchive
	 */
	public int getFilesPerArchive() {
		return filesPerArchive;
	}

	/**
	 * @param filesPerArchive the filesPerArchive to set
	 */
	public void setFilesPerArchive(int filesPerArchive) {
		this.filesPerArchive = filesPerArchive;
	}
	public void parseParameters(String fileName) throws ConfigurationException {
		load(fileName);
		// TODO add parsing
		// take care of all defaults
		// make sure that command-line parameters are more important
		// perhaps, output resulting parameter file with all defaults, for
		// future modifications and subsequent runs.
	}
}
