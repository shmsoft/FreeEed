package org.freeeed.main;

import java.io.IOException;

/**
 * Processing parameters
 */
public class FreeEedParam {
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
	public void parseParameters(String fileName) throws IOException {
		// TODO add parsing
		// take care of all defaults
		// make sure that command-line parameters are more important
		// perhaps, output resulting parameter file with all defaults, for
		// future modifications and subsequent runs.
	}
}
