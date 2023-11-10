/*
 *
 * Copyright SHMsoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.tika.metadata.Metadata;
import org.freeeed.main.DocumentMetadataKeys;
import org.freeeed.services.Project;
import org.freeeed.util.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColumnMetadata {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ColumnMetadata.class.getName());

    final private ArrayList<String> headers = new ArrayList<>();
    final private ArrayList<String> values = new ArrayList<>();

    private String fieldSeparator;
    private boolean isDatOutput = false;

    // allMetadata controls whether all or only standard mapped metadata is delivered
    private boolean allMetadata = false;
    private int standardHeaderSize = 0;
    private String datPrefix = "";
    private String datPostfix = "";

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
    private HashMap<String, String> aliases = new HashMap<>();

    public ColumnMetadata() {
        this(new DatabaseMetadataSource());
    }

    public ColumnMetadata(IMetadataSource metadataSource) {
        List<String> keys = metadataSource.getKeys();
        Collections.sort(keys);
        for (String key : keys) {
            String[] aka = metadataSource.getKeyValues(key);
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
            LOGGER.info("CREATED new header: " + header + "  " + hashCode());
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
                    && !Project.getCurrentProject().isTextInMetadata();
            if (exclude) {
                continue;
            }
            addMetadataValue(name, metadata.get(name));
        }
    }

    public String delimiterSeparatedValues() {
        StringBuilder builder = new StringBuilder();
        int headerCount = 0;
        boolean valueAdded = false;
        for (String value : values) {
            if (!allMetadata) {
                ++headerCount;
                if (headerCount > standardHeaderSize) {
                    continue;
                }
            }

            if (valueAdded) {
                builder.append(fieldSeparator);
            }

            builder.append(datPrefix).append(sanitize(value)).append(datPostfix);
            valueAdded = true;
        }
        return builder.toString();
    }

    /**
     * Generating the load file header
     *
     * @return String
     */
    public String delimiterSeparatedHeaders() {
        StringBuilder builder = new StringBuilder();
        boolean headerAdded = false;
        for (String header : headers) {

            if (headerAdded) {
                builder.append(fieldSeparator);
            }

            builder.append(datPrefix).append(sanitize(header)).append(datPostfix);
            headerAdded = true;
        }
        LOGGER.info(hashCode() + " > HEADERS: " + builder.toString());
        return builder.toString();
    }

    private String sanitize(String str) {
        if (str == null) return "";
        // replace all non-ascii with underscore
        String ascii = str.replaceAll("[^\\p{ASCII}]", "_");
        // replace all newlines with a space (we want everything on one line)
        ascii = ascii.replace("\n", " ");
        ascii = ascii.replace("\r", " ");
        //We only replace fieldSeparator occurrences only if we are not outputting DAT since DAT is to complicated to be in the text
        if (!isDatOutput) {
            // replace all occurrences of fieldSeparator with a space
            ascii = ascii.replace(fieldSeparator, " ");
        }
        // replace all occurrences of a quote with a space (we may enclose fields in quotes)
        ascii = ascii.replace("\"", " ");
        return ascii;
    }

    /**
     * @return the allMetadata
     */
    public boolean isAllMetadata() {
        return allMetadata;
    }

    /**
     * @param allMetadataStr the allMetadata to set
     */
    public void setAllMetadata(String allMetadataStr) {
        allMetadata = "ALL".equalsIgnoreCase(allMetadataStr);
    }

    public void setDatOutput(boolean datOutput) {
        isDatOutput = datOutput;
        fieldSeparator = "\u0014";
        datPrefix = "\u00FE";
        datPostfix = "\u00FE";
    }
}
