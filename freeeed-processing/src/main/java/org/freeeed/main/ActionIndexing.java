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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.freeeed.piranha.PreProcessor;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.SummaryMap;
import org.freeeed.services.Util;
import org.freeeed.ui.StagingProgressUI;
import org.freeeed.util.LogFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mark
 */
public class ActionIndexing implements Runnable {

    // TODO refactor downloading, eliminate potential UI thread locks
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ActionIndexing.class.getName());
    Project project = Project.getCurrentProject();
    /**
     * stagingUI call are GUI thread-safe
     */
    public ActionIndexing() {
    }

    @Override
    public void run() {
        try {
            indexForAI();
        } catch (Exception e) {
            LOGGER.severe("Error AI indexing");
        }
    }

    public void indexForAI() throws Exception {
    }
}
