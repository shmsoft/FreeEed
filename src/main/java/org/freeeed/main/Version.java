package org.freeeed.main;

import java.io.File;
import java.util.Date;

/**
 *
 * @author mark
 */
public class Version {
    public static final String version = ParameterProcessing.APP_NAME + " V3.6.6";
    
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
        String jarFileName = "target/" + ParameterProcessing.APP_NAME + "-1.0-SNAPSHOT-jar-with-dependencies.jar";
        File file = new File(jarFileName);
        if (file.exists()) {
            Date lastModified = new Date(file.lastModified());
            buildTime = lastModified.toString();
        }
        return buildTime;
    }
}
