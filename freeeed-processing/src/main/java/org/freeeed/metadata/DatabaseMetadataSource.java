package org.freeeed.metadata;

import java.util.ArrayList;
import java.util.List;

import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Metadata;

public class DatabaseMetadataSource implements IMetadataSource {

    private final Metadata metadata;

    public DatabaseMetadataSource() {
        try {
            metadata = DbLocalUtils.loadMetadata();
        } catch (Exception e) {
            throw new RuntimeException("Could not load metadata from database", e);
        }
    }

    @Override
    public List<String> getKeys() {
        return new ArrayList<>(metadata.stringPropertyNames());
    }

    @Override
    public String[] getKeyValues(String key) {
        String value = metadata.getProperty(key);
        String[] tokens = value.split(",");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }

}
