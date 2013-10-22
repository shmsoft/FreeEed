/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
/*    
    This file is part of SHMcloud.	

    Copyright 2012-2013 SHMsoft, Inc.

*/ 
package org.freeeed.main;

import java.io.File;
import java.util.Date;

/**
 *
 * @author mark
 */
public class Version {

    public static final String version = ParameterProcessing.APP_NAME + " V4.0.2";
    
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
