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
package org.freeeed.main;

import org.freeeed.data.index.ESIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class LoadEntryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataWriter.class);
    private String[] headers;
    private boolean firstLine = true;

    public void processLoadLine(String line) {
        String loadFileFormat = Project.getCurrentProject().getLoadFileFormat().toUpperCase();
        switch (loadFileFormat) {
            case "CSV":
                processLoadLineCSV(line);
                break;
            case "JSON":
                processLoadLineJson(line);
                break;
            default:
                LOGGER.error("Load file format incorrect");
        }
    }
    public void processLoadLineCSV(String line) {
        String[] fields = getFields(line);
        if (firstLine) {
            headers = fields;
            firstLine = false;
            return;
        }
        DocumentMetadata metadata = new DocumentMetadata();
        for (int i = 0; i < headers.length; ++i) {
            metadata.addField(headers[i], fields[i]);
        }
        
//        The code below would read the text if it were presentin staging, in the directory
//        But currently we expect the text to be in the "text" field
//        try {
//            File textFile = new File(metadata.getTextLink());
//            if (textFile.exists()) {
//                String text = Files.toString(textFile, Charset.defaultCharset());
//                metadata.setDocumentText(text);
//            }
//        } catch (IOException e) {
//            LOGGER.warn("Cannot read text while importing the load file", e);
//        }
        ESIndex.getInstance().addBatchData(metadata);
    }

    private String[] getFields(String line) {
        String sep = ",";
        String sepCode = Project.getCurrentProject().getFieldSeparator();
        if ("pipe".equalsIgnoreCase(sepCode)) {
            sep = "\\|";
        }
        return line.split(sep);
    }
    
    private void processLoadLineJson(String line) {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parseJsonFields(line, metadata);  
        ESIndex.getInstance().addBatchData(metadata);
    }
}
