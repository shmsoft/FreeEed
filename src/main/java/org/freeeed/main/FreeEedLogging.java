package org.freeeed.main;

import org.freeeed.services.Util;
import java.io.File;

public class FreeEedLogging {

    public static final String logDir = "logs";
    public static final String history = logDir + "/" + "processing_history.txt";
    public static final String stats = logDir + "/" + "stats.txt";

    public static void init() {
        if (Util.getEnv() == Util.ENV.LOCAL) {
            new File(logDir).mkdirs();
        }
    }
}
