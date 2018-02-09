package org.freeeed.dedup;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by nehaojha on 09/02/18.
 */
public class DeDuplicationTest {

    @Test
    public void testDuplicateFiles() throws Exception {
        long startTime = System.currentTimeMillis();
        Map<String, List<String>> groupedDuplicateFiles = new DuplicateFileAggregatorImpl().groupDuplicateFiles("../test-data");
        Assert.assertEquals(groupedDuplicateFiles.size(), 229);
//        printDuplicateFiles(groupedDuplicateFiles);
        System.out.println("time taken = " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void printDuplicateFiles(Map<String, List<String>> groupedDuplicateFiles) {
        groupedDuplicateFiles.forEach((k, v) -> {
            System.out.println(k + " has " + v.size() + " duplicates");
            System.out.println(v);
        });
    }
}