package org.freeeed.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.Project;

public class ColumnMetadata {

    private ArrayList<String> headers = new ArrayList<String>();
    private ArrayList<String> values = new ArrayList<String>();
    public static final String metadataNamesFile = "config/standard-metadata-names.properties";
    private FreeEedConfiguration metadataNames;
    private String fieldSeparator;
    // allMetadata controls whether all or only standard mapped metadata is delivered
    private boolean allMetadata = false;
    private int standardHeaderSize = 0;

    /**
     * @return the fieldSeparator
     */
    public String getFieldSeparator() {
        return fieldSeparator;
    }

    /**
     * @param fieldSeparator the fieldSeparator to set
     */
    public void setFieldSeparator(String fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }
    /**
     * Aliases give all name by which are metadata goes
     */
    private HashMap<String, String> aliases = new HashMap<String, String>();

    public ColumnMetadata() {
        init();
    }

    private void init() {
        try {
            metadataNames = new FreeEedConfiguration(metadataNamesFile);
        } catch (ConfigurationException e) {
            System.out.println("Error: file " + metadataNamesFile + " could not be read");
            e.printStackTrace(System.out);
        }
        Iterator numberKeys = metadataNames.getKeys();
        ArrayList<String> stringKeys = new ArrayList<String>();
        while (numberKeys.hasNext()) {
            stringKeys.add((String) numberKeys.next());
        }
        Collections.sort(stringKeys);
        for (String key : stringKeys) {
            String[] aka = metadataNames.getStringArray(key);
            String realName = aka[0];
            addMetadataValue(realName, "");
            // skip the  first one, which is the alias of itself
            for (int i = 1; i < aka.length; ++i) {
                String alias = aka[i];
                aliases.put(alias, realName);
            }
        }
        standardHeaderSize = headers.size();
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
            // exclude the text from metadadata - depending on the project's settings
            boolean exclude = name.equalsIgnoreCase(DocumentMetadataKeys.DOCUMENT_TEXT)
                    && !Project.getProject().isTextInMetadata();
            if (exclude) {
                continue;
            }
            addMetadataValue(name, metadata.get(name));
        }
    }

    public String delimiterSeparatedValues() {
        StringBuilder builder = new StringBuilder();
        int headerCount = 0;
        for (String value : values) {
            if (!allMetadata) {
                ++headerCount;
                if (headerCount > standardHeaderSize) {
                    continue;
                }
            }
            builder.append(fieldSeparator).append(sanitize(value));
        }
        return builder.toString();
    }

    /**
     * Why would this function be needed for anything but tab delimiter?
     *
     * @return
     */
    public String delimiterSeparatedHeaders() {
        StringBuilder builder = new StringBuilder();
        int headerCount = 0;
        for (String header : headers) {
            if (!allMetadata) {
                ++headerCount;
                if (headerCount > standardHeaderSize) {
                    continue;
                }
            }
            builder.append(fieldSeparator).append(sanitize(header));
        }
        return builder.toString();
    }

    private String sanitize(String str) {
        // replace all non-ascii with underscore
        String ascii = str.replaceAll("[^\\p{ASCII}]", "_");
        // replace all newlines with a space (we want everything on one line)
        ascii = ascii.replace("\n", " ");
        ascii = ascii.replace("\r", " ");
        // replace all occurences of fieldSeparator with a space
        ascii = ascii.replace(fieldSeparator, " ");
        return ascii;
    }

    /**
     * @return the allMetadata
     */
    public boolean isAllMetadata() {
        return allMetadata;
    }

    /**
     * @param allMetadata the allMetadata to set
     */
    public void setAllMetadata(String allMetadataStr) {
        this.allMetadata = "ALL".equalsIgnoreCase(allMetadataStr);
    }
}
