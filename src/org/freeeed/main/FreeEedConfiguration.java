package org.freeeed.main;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author mark
 */
public class FreeEedConfiguration extends PropertiesConfiguration {

    private Properties cache = new Properties();

    public FreeEedConfiguration() {
        super();
        //setProperty(ParameterProcessing.PROCESS_WHERE, ParameterProcessing.LOCAL);
    }

    public FreeEedConfiguration(String fileName) throws ConfigurationException {
        super(fileName);
        //setProperty(ParameterProcessing.PROCESS_WHERE, ParameterProcessing.LOCAL);
    }

    public void cleanup() {
        cache.clear();
        String projectFileName = (String) getProperty(ParameterProcessing.PROJECT_FILE_NAME);
        if (projectFileName != null) {
            cache.put(ParameterProcessing.PROJECT_FILE_NAME, projectFileName);
        }
        clearProperty(ParameterProcessing.PROJECT_FILE_NAME);
    }

    public void restore() {
        Enumeration keys = cache.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = cache.getProperty(key);
            setProperty(key, value);
        }        
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        Iterator iterator = getKeys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = (String) getProperty(key);
            str.append("key=").append(key).append(", value=").append(value).append("\n");
        }
        return str.toString();
    }
}
