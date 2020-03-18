package org.freeeed.quickbooks;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QBCsvParser {

    public static final int BATCH_SIZE = 100;

    public static void readCSVAsJson(String csvFile, String indicesName) {
        String line = "";
        String cvsSplitBy = ",";
        boolean firstLine = true;
        String[] header = new String[0];
        List<Map<String, String>> jsonMap = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^\\p{ASCII}]", "");
                String[] rowData = line.split(cvsSplitBy);
                trimQuotes(rowData);
                if (firstLine) {
                    header = rowData;
                    firstLine = false;
                } else {
                    Map<String, String> keyVal = mapRowToHeader(header, rowData);
                    jsonMap.add(keyVal);
                }
                if (jsonMap.size() == BATCH_SIZE) {
                    flushData(jsonMap, indicesName);
                }
            }
            if (!jsonMap.isEmpty()) {
                flushData(jsonMap, indicesName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flushData(List<Map<String, String>> jsonMap, String indicesName) {
        uploadJsonArrayToES(jsonMap, indicesName);
        jsonMap.clear();
    }

    private static void uploadJsonArrayToES(List<Map<String, String>> jsonMap, String indicesName) {
        //ESIndexUtil.uploadJsonArrayToES(jsonMap, indicesName);
       // System.out.println("jsonMap = " + jsonMap);
    }

    private static Map<String, String> mapRowToHeader(String[] header, String[] rowData) {
        Map<String, String> keyVal = new HashMap<>();
        if (rowData == null || rowData.length == 0 || header.length == 0) {
            return keyVal;
        }
        for (int i = 0; i < header.length; i++) {
            if (i >= rowData.length) {
                break;
            }
            if (!rowData[i].isEmpty()) {
                keyVal.put(header[i], rowData[i]);
            }
        }
        return keyVal;
    }

    private static void trimQuotes(String[] rowData) {
        for (int i = 0; i < rowData.length; i++) {
            String currentCell = rowData[i];
            if (currentCell.startsWith("\"")) {
                currentCell = currentCell.substring(1);
            }
            if (currentCell.endsWith("\"")) {
                currentCell = currentCell.substring(0, currentCell.length() - 1);
            }
            rowData[i] = currentCell;
        }
    }
}
