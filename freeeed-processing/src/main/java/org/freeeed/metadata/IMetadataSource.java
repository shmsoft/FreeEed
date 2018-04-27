package org.freeeed.metadata;

import java.util.List;

public interface IMetadataSource {

	List<String> getKeys();

	String[] getKeyValues(String key);
	
}
