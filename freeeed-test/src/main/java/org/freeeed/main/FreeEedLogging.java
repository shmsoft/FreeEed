package org.freeeed.main;

import java.io.File;
import org.freeeed.services.Project;

public class FreeEedLogging {

    public static final String logDir = "logs";
    public static final String history = logDir + "/" + "processing_history.txt";
    public static final String stats = logDir + "/" + "stats.txt";

    public static void init(boolean force) {
        if (Project.getProject().isEnvLocal() || force) {
            new File(logDir).mkdirs();
        }
    }
}
