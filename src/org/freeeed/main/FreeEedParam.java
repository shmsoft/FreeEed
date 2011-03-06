package org.freeeed.main;

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
}
