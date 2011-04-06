package org.freeeed.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.tika.metadata.Metadata;

public class ColumnMetadata {

    private ArrayList<String> headers = new ArrayList<String>();
    private ArrayList<String> values = new ArrayList<String>();
    private static final String metadataNamesFile = "standard.metadata.names.properties";
    private PropertiesConfiguration metadataNames;

    public ColumnMetadata() {
        init();
    }

    private void init() {
        try {
            metadataNames = new PropertiesConfiguration(metadataNamesFile);
        } catch (ConfigurationException e) {
            System.out.println("File " + metadataNamesFile + " could not be read");
            e.printStackTrace(System.out);
            System.exit(1);
        }
        Iterator keys = metadataNames.getKeys();
        ArrayList<String> names = new ArrayList<String>();
        while (keys.hasNext()) {
            names.add((String) keys.next());
        }
        Collections.sort(names);
        for (String name : names) {
            addMetadataValue(metadataNames.getString(name), "");
        }
    }

    /**
     * @return the values
     */
    public ArrayList<String> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(ArrayList<String> values) {
        this.values = values;
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
