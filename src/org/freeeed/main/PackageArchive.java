package org.freeeed.main;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Package the input directories into zip archives. Zip is selected
 * because it allows comments, which contain path, custodian, and later-
 * forensics information.
 */
public class PackageArchive {
	private int filesPerArchive;
	private ArrayList <String> inputDirs;
	private String rootDir = ".";
	
	// these are needed for the internal working of the code, not for outside
	private String packageFileNamePrefix = "input";
	private int packageFileCount = 0;
	private DecimalFormat packageFileNameFormat = new DecimalFormat("000");
	private String packageFileNameSuffix = ".zip";

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

	/**
	 * @return the inputDirs
	 */
	public ArrayList <String> getInputDirs() {
		return inputDirs;
	}

	/**
	 * @param inputDirs the inputDirs to set
	 */
	public void setInputDirs(ArrayList <String> inputDirs) {
		this.inputDirs = inputDirs;
	}

	/**
	 * @return the rootDir
	 */
	public String getRootDir() {
		return rootDir;
	}

	/**
	 * @param rootDir the rootDir to set
	 */
	public void setRootDir(String rootDir) {
		this.rootDir = rootDir;
	}
	public void packageArchive(String dir) throws IOException {
		
	}
	
}
