package org.freeeed.dedup;

import org.freeeed.main.DocumentMetadata;

import java.util.List;

/**
 * Created by nehaojha on 09/02/18.
 */
public interface DuplicateFileAggregator {

    List<DocumentMetadata> groupDuplicateFiles(String directoryPath) throws Exception;

}
