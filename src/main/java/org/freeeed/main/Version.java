package org.freeeed.main;

import java.io.File;
import java.util.Date;

/**
 *
 * @author mark
 */
public class Version {
    public static final String version = "FreeEed V3.5.7";
    
    public static String getVersionAndBuild() {
        return version
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
