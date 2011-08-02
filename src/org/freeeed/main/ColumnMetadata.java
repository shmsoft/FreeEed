package org.freeeed.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tika.metadata.Metadata;

public class ColumnMetadata {

    private ArrayList<String> headers = new ArrayList<String>();
    private ArrayList<String> values = new ArrayList<String>();
    private static final String metadataNamesFile = "config/standard.metadata.names.properties";
    private PropertiesConfiguration metadataNames;
    /**
     * Aliases give all name by which are metadata goes
     */
    private HashMap <String, String> aliases = new HashMap<String, String>();
    public ColumnMetadata() {
        init();
    }

    private void init() {
        try {
            metadataNames = new PropertiesConfiguration(metadataNamesFile);
        } catch (ConfigurationException e) {
            System.out.println("Error: file " + metadataNamesFile + " could not be read");
            e.printStackTrace(System.out);
            System.exit(1);
        }
        Iterator numberKeys = metadataNames.getKeys();
        ArrayList<String> stringKeys = new ArrayList<String>();
        while (numberKeys.hasNext()) {
            stringKeys.add((String) numberKeys.next());
        }
        Collections.sort(stringKeys);
        for (String key: stringKeys) {
            String[] aka = metadataNames.getStringArray(key);
            String realName = aka[0];
            addMetadataValue(realName, "");
            // skip the  first one, which is the alias of itself
            for (int i = 1; i < aka.length; ++i) {
                String alias = aka[i];                    
                aliases.put(alias, realName);
            }            
        }
    }

    public void reinit() {
        for (int i = 0; i < values.size(); ++i) {
            values.set(i, "");
        }
    }
    public void addMetadataValue(String header, String value) {
        // if we have this header, put the value in the right place
        if (headers.contains(header)) {
            int index = headers.indexOf(header);
            values.set(index, value);

        } else { // if we don't have such a header, add it
            headers.add(header);
            values.add(value);
        }
        // additionally, map every alias to the real name
        if (aliases.containsKey(header)) {
            String realName = aliases.get(header);
            addMetadataValue(realName, value);
        }
    }

    public void addMetadata(Metadata metadata) {
        String[] names = metadata.names();
        for (String name : names) {
            if (!name.equalsIgnoreCase(DocumentMetadataKeys.DOCUMENT_TEXT)) {
                addMetadataValue(name, metadata.get(name));
            }
        }
    }

    public String tabSeparatedValues() {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            builder.append("\"").append(value).append("\"").append("\t");
        }
        return builder.toString();
    }

    public String tabSeparatedHeaders() {
        StringBuilder builder = new StringBuilder();
        for (String header : headers) {
            builder.append(header).append("\t");
        }
        return builder.toString();
    }
}
