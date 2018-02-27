package org.freeeed.dedup;

import org.freeeed.main.DocumentMetadata;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by nehaojha on 09/02/18.
 */
public class DeDuplicationTest {

    public void testDuplicateFiles() throws Exception {
        long startTime = System.currentTimeMillis();
        List<DocumentMetadata> documentMetadataList = new DuplicateFileAggregatorImpl().groupDuplicateFiles("../test-data");
//        printDuplicateFiles(documentMetadataList);
        System.out.println("time taken = " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void printDuplicateFiles(List<DocumentMetadata> documentMetadataList) {
        Map<String, List<DocumentMetadata>> groupedByHash = documentMetadataList.stream().collect(Collectors.groupingBy(DocumentMetadata::getHash));
        groupedByHash.forEach((k, v) -> {
            if (v.size() > 1) {
                System.out.println(k);
                System.out.println(v);
            }
            System.out.println("============================================================");
        });
    }
}