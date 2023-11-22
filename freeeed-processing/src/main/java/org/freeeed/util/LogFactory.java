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
package org.freeeed.util;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

public class LogFactory {
    public static final String LOG_FILE_DIR = "logs/";
    public static final String LOG_FILE_NAME = "FreeEed.log";
    private static Logger logger = null;
    static {
        try {
            Path pathToDirectory = Paths.get(LOG_FILE_DIR);
            Files.createDirectories(pathToDirectory);
            logger = Logger.getLogger("FreeEed");
            configureLogger(logger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Get a logger for the given class.
     * @param name not used
     * @return logger fpr everyone
     */
    public static Logger getLogger(String name) {
//        if (logger == null) {
//            synchronized (LogFactory.class) {
//                if (logger == null) {
//                    logger = Logger.getLogger("FreeEed");
//                    configureLogger(logger);
//                }
//            }
//        }
        return logger;
    }

    private static void configureLogger(Logger logger) {
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE_DIR + LOG_FILE_NAME, 0, 1, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}