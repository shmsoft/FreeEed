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

import java.io.IOException;

import org.freeeed.util.LogFactory;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class Services {

    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(Services.class.getName());

    private static boolean local = true;
    
    // TODO verify on Windows for path problems


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
