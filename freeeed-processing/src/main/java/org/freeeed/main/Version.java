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
package org.freeeed.main;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;


/**
 *
 * @author mark
 */
public class Version {
    private static String V = "0.0.0";
    static {
        try {
            V = Files.toString(new File("VersionNumber.txt"), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        };
    }
    public static String getVersionAndBuild() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber()                
                + "Build date: " + getBuildTime();
    }       

    private static String getBuildTime() {
        String buildTime = "Unknown";
        String jarFileName = "target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar";
        File file = new File(jarFileName);
        if (file.exists()) {
            Date lastModified = new Date(file.lastModified());
            buildTime = lastModified.toString();
        }
        return buildTime;
    }
    
    public static String getVersion() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber();
    }
    
    private static String getVersionNumber() {
        return V;
    }
}
