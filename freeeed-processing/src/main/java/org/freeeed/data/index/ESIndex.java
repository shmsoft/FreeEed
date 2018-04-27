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
package org.freeeed.data.index;

import org.apache.tika.metadata.Metadata;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Create ES index.
 * <p>
 *
 * @author ivanl
 */
public abstract class ESIndex {

    private static final Logger logger = LoggerFactory.getLogger(ESIndex.class);
    public static final String ES_INSTANCE_DIR = "shmcloud";
    private static ESIndex instance;
    protected boolean isInited = false;

    public static synchronized ESIndex getInstance() {
        if (instance == null) {
            if (Project.getCurrentProject().isSendIndexToESEnabled()) {
                logger.debug("ESIndex Create HttpESIndex");
                instance = new HttpESIndex();
            } else {
                logger.debug("ESIndex Create DisabledESIndex");
                instance = new DisabledESIndex();
            }
        }
        return instance;
    }

    public abstract void addBatchData(Metadata metadata);

    public abstract void init();

    public void destroy() {
        synchronized (ESIndex.class) {
            instance = null;
        }
    }

    private static final class HttpESIndex extends ESIndex {

        @Override
        public synchronized void addBatchData(Metadata metadata) {
            createESDoc(metadata);
        }

        public void createESDoc(Metadata metadata) {
            Map<String, Object> jsonMap = new HashMap<>();
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                String data = metadata.get(name);
                jsonMap.put(name, filterNotCorrectCharacters(data));
            }
            String projectCode = Project.getCurrentProject().getProjectCode();
            ESIndexUtil.addDocToES(jsonMap, ES_INSTANCE_DIR + "_" + projectCode, metadata.get(DocumentMetadata.UNIQUE_ID));
        }

        private String filterNotCorrectCharacters(String data) {
            return data.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "").replaceAll("]]", "");
        }

        @Override
        public void init() {
            isInited = true;
            String projectCode = Project.getCurrentProject().getProjectCode();
            ESIndexUtil.createIndices(ES_INSTANCE_DIR + "_" + projectCode);
        }
    }

    private static final class DisabledESIndex extends ESIndex {

        @Override
        public void addBatchData(Metadata metadata) {
            //do nothing
        }

        @Override
        public void init() {
            // do nothing
        }

    }
}
