package org.freeeed.helpers;

/**
 * Created by nehaojha on 01/02/18.
 */
public interface StagingProgressUIHelper {

    void setDownloadingState();

    void setPackagingState();

    void resetCurrentSize();

    void setTotalSize(long total);

    void updateProcessingFile(final String fileName);

    void updateProgress(long size);

    void setDone();

    void setPreparingState();
}
