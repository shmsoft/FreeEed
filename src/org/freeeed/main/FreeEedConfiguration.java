package org.freeeed.main;

import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 *
 * @author mark
 */
public class FreeEedConfiguration extends PropertiesConfiguration {
	public FreeEedConfiguration() {
		super();
		//setProperty(ParameterProcessing.PROCESS_WHERE, ParameterProcessing.LOCAL);
	}
	public FreeEedConfiguration(String fileName) throws ConfigurationException {
		super(fileName);
		//setProperty(ParameterProcessing.PROCESS_WHERE, ParameterProcessing.LOCAL);
	}
	public void cleanup() {
		setProperty(ParameterProcessing.PROJECT_FILE_NAME, null);	
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
