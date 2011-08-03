/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.main;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.freeeed.util.History;

/**
 *
 * @author mark
 */
public class ActionProcessing implements Runnable {
	private String runWhere;
	
	public ActionProcessing(String runWhere) {
		this.runWhere = runWhere;
	}
	
	@Override
	public void run() {
		try {
			process();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	public void process() throws Exception {
		Configuration processingParameters = FreeEedMain.getInstance().getProcessingParameters();
		History.appendToHistory("Processing project: " + processingParameters.getString(ParameterProcessing.PROJECT_NAME));
		System.out.println("Processing: " + runWhere);
		ParameterProcessing.echoProcessingParameters(processingParameters);
		if (ParameterProcessing.LOCAL.equals(runWhere)) {
			try {
				String[] processingArguments = new String[1];				
				processingArguments[0] = processingParameters.getString("output-dir");
				if (new File(processingArguments[0]).exists()) {
					System.out.println("Please remove output directory " + processingArguments[0]);
					System.out.println("For example, in Linux you can do rm -fr " + processingArguments[0]);
					throw new RuntimeException("Output directory not empty");
				}
				FreeEedProcess.main(processingArguments);
			} catch (Exception e) {
				e.printStackTrace(System.out);
				throw new FreeEedException(e.getMessage());
			}
		}		
		
		History.appendToHistory("Done");
	}
}
