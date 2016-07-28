package org.freeeed.metadata;

import java.util.List;

public interface IMetadataSource {

	public List<String> getKeys();

	public String[] getKeyValues(String key);
	
}
