package org.freeeed.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.freeeed.main.FreeEedConfiguration;

public class PropertiesFileMetadataSource implements IMetadataSource {

    public static final String METADATA_FILENAME = "config/standard-metadata-names.properties";
    
	private final FreeEedConfiguration configuration;
	
	private List<String> keys;
	
	public PropertiesFileMetadataSource() {
		try {
			configuration = new FreeEedConfiguration(METADATA_FILENAME);
		} catch (ConfigurationException e) {
			System.out.println("Error: file " + METADATA_FILENAME + " could not be read");
			throw new RuntimeException(e);
		}
		Iterator<String> numberKeys = configuration.getKeys();
		this.keys = new ArrayList<>();
		while (numberKeys.hasNext()) {
			keys.add((String) numberKeys.next());
		}
	}
	
	@Override
	public List<String> getKeys() {
		return keys;
	}

	@Override
	public String[] getKeyValues(String key) {
		return configuration.getStringArray(key);
	}
	
}
