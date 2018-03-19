package org.freeeed.helpers;

/**
 * Created by nehaojha on 01/02/18.
 */
public interface ProcessProgressUIHelper {

    ProcessProgressUIHelper getInstance();

    void setDone();

    void setProcessingState(String fileName);

    void setTotalSize(long total);

    void updateProgress(long size);
}
