package org.freeeed.helpers;

public interface FreeEedUIHelper {
    void setProgressBarMaximum(int max);
    void setProgressBarValue(int prg);
    void setProgressLabel(String label);
    void setProgressDone();
    void setProgressedSize(String label);
    void setProgressIndeterminate(boolean status);
}
