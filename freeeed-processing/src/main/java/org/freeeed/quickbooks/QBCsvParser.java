package org.freeeed.quickbooks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.freeeed.data.index.ESIndexUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QBCsvParser {

    public static final int BATCH_SIZE = 100;

    public static void main(String[] args) throws Exception {
//        String csvFile = "/Users/nehaojha/Downloads/qbformat/Chariman.CSV";
//        ESIndexUtil.createIndices("test");
//        readCSVAsJson(csvFile);
    }

    private static void readCSVAsJson(String csvFile) {
        String line = "";
        String cvsSplitBy = ",";
        boolean firstLine = true;
        String[] header = new String[0];
        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonArray = new ArrayList<>();

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
                    jsonArray.add(mapper.writeValueAsString(keyVal));
                }
                if (jsonArray.size() == BATCH_SIZE) {
                    flushData(mapper, jsonArray);
                }
            }
            if (!jsonArray.isEmpty()) {
                flushData(mapper, jsonArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void flushData(ObjectMapper mapper, List<String> jsonArray) throws JsonProcessingException {
        uploadJsonArrayToES(jsonArray);
        jsonArray.clear();
    }

    private static void uploadJsonArrayToES(List<String> jsonArray) {
        ESIndexUtil.uploadJsonArrayToES(jsonArray, "test");
        System.out.println("jsonArray = " + jsonArray);
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
