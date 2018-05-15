package org.freeeed.data.index;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;

import java.util.Map;

/**
 * Created by nehaojha on 08/04/18.
 */
public class ESIndexUtil {

    private static final Logger LOGGER = Logger.getLogger(ESIndexUtil.class);

    public static void createIndices(String indicesName) {
        if (!Project.getCurrentProject().isSendIndexToESEnabled()) {
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())))) {
                CreateIndexRequest request = new CreateIndexRequest(indicesName);
                client.indices().create(request);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    public static void addDocToES(Map<String, Object> jsonMap, String indicesName, String id) {
        if (!Project.getCurrentProject().isSendIndexToESEnabled()) {
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())))) {
                IndexRequest indexRequest = new IndexRequest(indicesName, indicesName, id)
                        .source(jsonMap);
                client.index(indexRequest);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }

    public static void addBlockChainToES(String data, String indicesName, int blockNumber) {
        System.out.println("data = " + data);
        if (!Project.getCurrentProject().isSendIndexToESEnabled()) {
            LOGGER.info("searching not enabled returning,..");
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(HttpHost.create(Settings.getSettings().getESEndpoint())))) {
                IndexRequest indexRequest = new IndexRequest(indicesName, indicesName, "" + blockNumber)
                        .source(data, XContentType.JSON);
                client.index(indexRequest);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }
}
