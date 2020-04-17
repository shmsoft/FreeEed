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

/**
 * @author mark
 */
public class Version {
    private static final String V = "8.0.1";
    private static final String BuildNumber = "27";

    public static String getVersionAndBuild() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber() + " Build " + getBuildNumber();
    }

    private static String getBuildNumber() {
        return BuildNumber;
    }

    public static String getVersion() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber();
    }

    private static String getVersionNumber() {
        return V;
    }
}
