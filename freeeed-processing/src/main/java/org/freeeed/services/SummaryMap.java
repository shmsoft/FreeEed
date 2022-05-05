package org.freeeed.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SummaryMap extends HashMap<String, Long> {
    public void addToSummaryMap(File fileName) {
        String extension = Util.getExtension(fileName.getName());
        Map<String, Long> summaryMap = Project.getCurrentProject().getSummaryMap();
        if (summaryMap.containsKey(extension)) {
            long count = summaryMap.get(extension);
            summaryMap.put(extension, count + 1);
        } else {
            summaryMap.put(extension, 1L);
        }
    }

    public long getTotalFiles() {
        long totalCount = 0;
        for (Map.Entry<String, Long> set :
                entrySet()) {
            totalCount += set.getValue();
        }
        return totalCount;
    }
}
