package org.freeeed.dedup;

import java.util.List;
import java.util.Map;

/**
 * Created by nehaojha on 09/02/18.
 */
public interface DuplicateFileAggregator {

    Map<String, List<String>> groupDuplicateFiles(String directoryPath) throws Exception;

}
