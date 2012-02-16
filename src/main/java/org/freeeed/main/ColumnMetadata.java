package org.freeeed.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.tika.metadata.Metadata;

public class ColumnMetadata {

    private String loadFormat;
    private ArrayList<String> headers = new ArrayList<String>();
    private ArrayList<String> values = new ArrayList<String>();
    public static final String metadataNamesFile = "standard-metadata-names.properties";
    private FreeEedConfiguration metadataNames;
    char tab = '\t';
    char one = '\u0001';

    /**
     * @return the loadFormat
     */
    public String getLoadFormat() {
        return loadFormat;
    }

    /**
     * @param loadFormat the loadFormat to set
     */
    public void setLoadFormat(String loadFormat) {
        this.loadFormat = loadFormat;
        if ("csv".equalsIgnoreCase(loadFormat)) {
            setDelim(DELIM.TAB_DELIM);
        } else if ("hive".equalsIgnoreCase(loadFormat)) {
            setDelim(DELIM.HIVE_DELIM);
        }

    }

    public enum DELIM {

        TAB_DELIM, HIVE_DELIM
    };
    private DELIM delim = DELIM.TAB_DELIM;
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

    public String delimiterSeparatedValues() {
        StringBuilder builder = new StringBuilder();
        int header = 0;        
        for (String value : values) {
            if (loadFormat.equalsIgnoreCase("hive")) {
                ++header;
                if (header > headers.size()) {
                    continue;
                }
            }
            if (delim == DELIM.TAB_DELIM) {
                
                builder.append("\"").append(sanitize(value)).append("\"").append(tab);
            } else if (delim == DELIM.HIVE_DELIM) {
                builder.append(one).append(value);
            }
        }
        if (delim == DELIM.HIVE_DELIM) {
            builder.append(one);
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
        for (String header : headers) {            
            if (delim == DELIM.TAB_DELIM) {
                builder.append(header).append(tab);
            } else if (delim == DELIM.HIVE_DELIM) {
                builder.append(one).append(header);
            }
        }
        if (delim == DELIM.HIVE_DELIM) {
            builder.append(one);
        }
        return builder.toString();
    }

    /**
     * @return the delim
     */
    public DELIM getDelim() {
        return delim;
    }

    /**
     * @param delim the delim to set
     */
    public void setDelim(DELIM delim) {
        this.delim = delim;
    }
    private String sanitize(String str) {        
        // replace all non-ascii with underscore
        String ascii = str.replaceAll("[^\\p{ASCII}]", "_");
        // replace all newlines with a space
        ascii = ascii.replace(System.getProperty("line.separator"), " ");
        return ascii;
    }
}
