package org.freeeed.helpers;

/**
 * Created by nehaojha on 01/02/18.
 */
public class ProgressBar {

    private static ProcessProgressUIHelper UI_HELPER;

    private ProgressBar() {
    }

    public static void initialize(ProcessProgressUIHelper processProgressUIHelper) {
        UI_HELPER = processProgressUIHelper;
    }

    public static ProcessProgressUIHelper getUiHelper() {
        return UI_HELPER;
    }
}
