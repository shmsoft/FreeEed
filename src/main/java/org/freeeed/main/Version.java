package org.freeeed.main;

import java.io.File;
import java.util.Date;

/**
 *
 * @author mark
 */
public class Version {

    public static String getVersion() {
        return "FreeEed V3.1.4"
                + "\n"
                + "Build time: " + getBuildTime();
    }

    public static String getSupportEmail() {
        return "freeeed@shmsoft.com";
    }

    public static String getBuildTime() {
        String buildTime = "Unknown";
        String jarFileName = "target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar";
        File file = new File(jarFileName);
        if (file.exists()) {
            Date lastModified = new Date(file.lastModified());
            buildTime = lastModified.toString();
        }
        return buildTime;
    }
}
