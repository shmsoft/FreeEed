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

import org.apache.http.HttpHost;
import org.apache.tika.metadata.Metadata;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create ES index.
 * <p>
 *
 * @author ivanl
 */
public class ESIndex {

    private static final Logger logger = LoggerFactory.getLogger(ESIndex.class);
    public static final String ES_INSTANCE_DIR = "freeeed";
    private static volatile ESIndex mInstance;
    private Project project = Project.getCurrentProject();
    private RestHighLevelClient client;
    private boolean isInit = false;

    private ESIndex() {
    }

    public static ESIndex getInstance() {
        if (mInstance == null) {
            synchronized (ESIndex.class) {
                if (mInstance == null) {
                    mInstance = new ESIndex();
                }
            }
        }
        return mInstance;
    }

    public synchronized void addBatchData(Metadata metadata,boolean isAsync) {
        createESDoc(metadata,isAsync);
    }

    private void createESDoc(Metadata metadata,boolean isAsync) {
        Map<String, Object> jsonMap = new HashMap<>();
        String[] metadataNames = metadata.names();
        for (String name : metadataNames) {
            String data = metadata.get(name);
            jsonMap.put(name, filterNotCorrectCharacters(data));
        }
        String projectCode = Project.getCurrentProject().getProjectCode();
        addDocToES(jsonMap, ES_INSTANCE_DIR + "_" + projectCode, metadata.get(DocumentMetadata.UNIQUE_ID),isAsync);
    }

    private String filterNotCorrectCharacters(String data) {
        return data.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "").replaceAll("]]", "");
    }

    public void init() {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())));
        createIndices(ES_INSTANCE_DIR + "_" + project.getProjectCode());
        isInit = true;
    }

    private void createIndices(String indicesName) {
        try {
            GetIndexRequest gR = new GetIndexRequest();
            gR.indices(indicesName);
            if (!client.indices().exists(gR)) {
                CreateIndexRequest request = new CreateIndexRequest(indicesName);
                client.indices().create(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDocToES(Map<String, Object> jsonMap, String indicesName, String id,boolean isAsync) {
        if (isInit) {
            try {
                IndexRequest indexRequest = new IndexRequest(indicesName, indicesName, id).source(jsonMap);
                if(isAsync){
                    client.indexAsync(indexRequest, null);
                }else{
                    client.index(indexRequest);
                }
            } catch (Exception ex) {
                logger.error(String.valueOf(ex));
                ex.printStackTrace();
            }
        }
    }

    public void addBlockChainToES(String data, String indicesName, int blockNumber) {
        /*
        logger.info("data = " + data);
        if (!Project.getCurrentProject().isSendIndexToESEnabled()) {
            logger.info("searching not enabled returning,..");
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())))) {
                IndexRequest indexRequest = new IndexRequest(indicesName, indicesName, "" + blockNumber)
                        .source(data, XContentType.JSON);
                client.index(indexRequest);
            }
        } catch (Exception ex) {
            logger.error(String.valueOf(ex));
        }
        */
    }

    public void uploadJsonArrayToES(List<Map<String, String>> jsonMap, String indicesName) {
        /*
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())))) {
                BulkRequest request = new BulkRequest();
                for (Map<String, String> map : jsonMap) {
                    String nextId = UniqueIdGenerator.INSTANCE.getNextDocumentId();
                    map.put("upi", nextId);
                    request.add(new IndexRequest(indicesName, indicesName, nextId)
                            .source(map));
                }
                client.bulk(request);
            }
        } catch (Exception ex) {
            logger.error(String.valueOf(ex));
        }
        */
    }

}
