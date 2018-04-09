/**
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
/**
 * Start services needed by FreeEed. The services will exit when the program exits
 */
package org.freeeed.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class Services {

    private static final Logger logger = LoggerFactory.getLogger(Services.class);
    private static boolean aws = false;
    private static boolean local = true;
    
    // TODO verify on Windows for path problems
    public static void start() {
        // TODO put it in a thread?
        logger.info("Starting services");
//        try {
//            OsUtil.runCommand("java -Djava.library.path=lib/DynamoDBLocal_lib -jar lib/DynamoDBLocal.jar -sharedDb",
//                    false, Long.MAX_VALUE);
//        } catch (IOException e) {
//            logger.error("Error starting services ", e);
//        }
    }


    private static final TaskQueue taskQueue = initTaskQueue();

    private static TaskQueue initTaskQueue() {
        if (Services.isLocal()) {
            return new LocalTaskQueue();
        } else if (Services.isAws()) {
            return new AwsTaskQueue();
        } else {
            return null;
        }
    }
    public static TaskQueue taskQueue() {
        return taskQueue;
    }

    /**
     * @return the aws
     */
    public static boolean isAws() {
        return aws;
    }

    /**
     * @param b the value of aws to set
     */
    public static void setAws(boolean b) {
        aws = b;
    }

    /**
     * @return the local
     */
    public static boolean isLocal() {
        return local;
    }

    /**
     * @param b the value of local to set
     */
    public static void setLocal(boolean b) {
        local = b;
    }
}
