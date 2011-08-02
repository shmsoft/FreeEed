package org.freeeed.main;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.freeeed.util.History;

public class ParameterProcessing {
    private static final String defaultParameterFile = "config/default.freeeed.properties";
    public static final String FILES_PER_ZIP_STAGING = "files-per-zip-staging";
    public static final String PROJECT_NAME = "project-name";
	public static final String PROJECT_FILE_NAME = "project-file-name";
	public static final String PROJECT_INPUTS = "input";
	public static final String PROJECT_CUSTODIANS = "custodian";
    
    public static Configuration collectProcessingParameters(String customParametersFile) {
        CompositeConfiguration cc = new CompositeConfiguration();		
        try {
            // custom parameter file is first priority
            if (customParametersFile != null) {
                Configuration customProperties = new PropertiesConfiguration(customParametersFile);
                cc.addConfiguration(customProperties);
            }
            // default parameter file is last priority
            Configuration defaults = new PropertiesConfiguration(defaultParameterFile);
            cc.addConfiguration(defaults);
			cc.setProperty(PROJECT_FILE_NAME, customParametersFile);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // follow the "fail-fast" design pattern
            System.exit(0);
        }
        return cc;
    }
	
    public static Configuration setDefaultParameters() {
        CompositeConfiguration cc = new CompositeConfiguration();		
        try {            
            Configuration defaults = new PropertiesConfiguration(defaultParameterFile);
            cc.addConfiguration(defaults);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // follow the "fail-fast" design pattern
            System.exit(0);
        }
        return cc;
    }
	

    public static void echoProcessingParameters(Configuration configuration)
            throws ConfigurationException, MalformedURLException {
        SimpleDateFormat fileNameFormat = new SimpleDateFormat(
                "yyMMdd_HHmmss");
        String runParameterFileName = "freeeed.parameters."
                + fileNameFormat.format(new Date()) + ".properties";
        PropertiesConfiguration configToSave = new PropertiesConfiguration();
        configToSave.append(configuration);
        new File(FreeEedLogging.logDir).mkdirs();
        String paramPath = FreeEedLogging.logDir + "/" + runParameterFileName;
        configToSave.save(paramPath);
        History.appendToHistory("Processing parameters were saved to " + paramPath);
    }
    
}
