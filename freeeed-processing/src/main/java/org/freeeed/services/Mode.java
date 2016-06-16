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
package org.freeeed.services;

import java.io.IOException;
import org.freeeed.db.DbLocal;

/**
 * The first basic piece of information: mode in LOCAL mode everything happens
 * on the localhost server in AWS mode, project information, data, and
 * computations happen on AWS
 */
public class Mode {

    
    private static final Mode instance = new Mode();

    private Mode() {
    }
    

    public static Mode getInstance() {
        return instance;
    }

    public enum RUN_MODE {
        LOCAL, AWS
    };
    private RUN_MODE runMode;

    /**
     * @return the mode
     */
    public RUN_MODE getRunMode() {
        return runMode;
    }

    /**
     * @param runMode
     */
    public void setRunMode(RUN_MODE runMode) {
        this.runMode = runMode;
    }
    public static void load() throws IOException {
        DbLocal.getInstance().loadMode();
    }
}
