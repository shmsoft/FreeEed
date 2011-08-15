/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.main;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.freeeed.services.History;

/**
 *
 * @author mark
 */
public class ActionStaging implements Runnable {

	@Override
	public void run() {
		try {
			stagePackageInput();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public void stagePackageInput() throws Exception {
		Configuration processingParameters = FreeEedMain.getInstance().getProcessingParameters();
		History.appendToHistory("Project: " + processingParameters.getString(ParameterProcessing.PROJECT_NAME));
		// TODO better setting of dirs?
		String stagingDir = PackageArchive.stagingDir;
		LinuxUtil.runLinuxCommand("rm -fr " + stagingDir);
		new File(stagingDir).mkdirs();
		
		String[] dirs = processingParameters.getStringArray(ParameterProcessing.PROJECT_INPUTS);		
		History.appendToHistory("Packaging and staging the following directories for processing:");
		PackageArchive packageArchive = new PackageArchive();
		// TODO - set custom packaging parameters		
		try {

			for (String dir : dirs) {
				History.appendToHistory(dir);
				packageArchive.packageArchive(dir);
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			// follow the "fail-fast" design pattern
			System.exit(0);
		}
		PackageArchive.writeInventory();
		History.appendToHistory("Done");
	}
}
