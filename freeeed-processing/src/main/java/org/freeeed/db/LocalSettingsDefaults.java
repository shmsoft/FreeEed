/*
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.db;

import org.freeeed.main.ParameterProcessing;

/**
 * Values for the local db to be put into the db on the first run of the program
 *
 * @author mark
 */
class LocalSettingsDefaults {

    // TODO - field names should be constants in some class like ParameterProcessing
    private final static String[][] initProperties
            = {
                {"instance-type", "c1.medium"},
                // TODO - this should be by project, no?
                {"process-where", "local"},
                {"availability-zone", "us-east-1a"},
                {"cluster-timeout", "5"},
                {"manual-page", "https://github.com/markkerzner/FreeEed/wiki"},
                //Release
                {"download-link", "http://shmsoft.s3.amazonaws.com/releases/FreeEed-5.1.0.zip"},
                {"items-per-mapper", "5000"},
                {"bytes-per-mapper", "250000000"},
                {"ami", "ami-36d42e5e"},
                {"project-bucket", "freeeed_projects"},
                {"review_endpoint", "http://localhost:7845/freeeedreview"},
                {"es_endpoint", "http://localhost:9200"},
                // defines the maximum size of a file that the application can process
                {"file_max_size_mb", "1024"},
                { ParameterProcessing.PROCESS_TIMEOUT_SEC, "300"}
            };

    public static String[][] getInitProperties() {
        return initProperties;
    }

}
