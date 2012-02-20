package org.freeeed.main;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.freeeed.services.History;

/**
 * Thread that configures Hadoop and performs data search
 *
 * @author mark
 */
public class ActionProcessing implements Runnable {

    private String runWhere;

    /**
     * @param runWhere determines whether Hadoop runs on EC2, local cluster, or local machine
     */
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

    /**
     * @throws Exception
     */
    public void process() throws Exception {
        Configuration processingParameters = FreeEedMain.getInstance().getProcessingParameters();
        
        History.appendToHistory("Processing project: " + processingParameters.getString(ParameterProcessing.PROJECT_NAME));
       
        System.out.println("Processing: " + runWhere);
        Util.setEnv(runWhere);
        
        ParameterProcessing.echoProcessingParameters(processingParameters);

        // this code only deals with local Hadoop processing
        if (Util.getEnv() == Util.ENV.LOCAL) {
            try {
                // check output directory
                String[] processingArguments = new String[2];
                processingArguments[0] = ParameterProcessing.getInventoryFileName();
                processingArguments[1] = ParameterProcessing.getResultsDir();
                // check if output directory exists
                if (new File(processingArguments[1]).exists()) {
                    System.out.println("Please remove output directory " + processingArguments[0]);
                    System.out.println("For example, in Unix you can do rm -fr " + processingArguments[0]);
                    throw new RuntimeException("Output directory not empty");
                }
                MRFreeEedProcess.main(processingArguments);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new FreeEedException(e.getMessage());
            }
        }

        History.appendToHistory("Done");
    }
}
