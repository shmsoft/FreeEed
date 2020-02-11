package org.freeeed.helpers;

public interface FreeEedUIHelper {
    void setScaiaStatus(boolean status,boolean logged);
    void setScaiaStatus(boolean status);
    void setProgressBarMaximum(int max);
    void setProgressBarValue(int prg);
    void setProgressLabel(String label);
    void setProgressDone();
    void setTotalProgressSize(long size);
    void setProgressedSize(String label);
    void setProgressIndeterminate(boolean status);
}
