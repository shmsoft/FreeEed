package org.freeeed.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.Files;
import org.freeeed.main.FreeEedMain;

public class CsvMetadataParser {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(FreeEedMain.class.getName());
    private String delim;
    
    public CsvMetadataParser(String delim) {
        this.delim = delim;
    }
    
    /**
     * 
     * Parse the given csv file to map.
     * The map structure is - key: File Name, value: Map with key: column, value: the real value
     * 
     * @param fileName file to parse.
     * @return parsed metadata, or null if not unable to parse.
     */
    public Map<String, Map<String, String>> parseFile(String fileName) {
        File csvFile = new File(fileName);
        try {
            List<String> lines = Files.readLines(csvFile, Charset.forName("UTF-8"));
            return parseLines(lines);
        } catch (IOException e) {
            LOGGER.severe("Problem parsing file: " + e);
        }
        
        return null;
    }
    
    public Map<String, Map<String, String>> parseLines(List<String> lines) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Map<Integer, String> columnNames = new HashMap<>();
        
        int rowNum = 0;
        
        for (String line : lines) {
            String[] columns = line.split(delim);
            
            if (rowNum == 0) {
                for (int i = 0; i < columns.length; i++) {
                    columnNames.put(i, columns[i]);
                }    
            } else {
                Map<String, String> values = new HashMap<>();
                for (int i = 0; i < columns.length; i++) {
                    values.put(columnNames.get(i), columns[i]);
                }
                
                result.put(values.get("File Name"), values);
            }
            
            rowNum++;
        }
        
        return result;
    }
}
