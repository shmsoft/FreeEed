package org.freeeed.data.index;

import org.apache.http.HttpHost;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.Map;

/**
 * Created by nehaojha on 08/04/18.
 */
public class ESIndexUtil {

    private static final Logger LOGGER = Logger.getLogger(ESIndexUtil.class);
    private static final boolean enabled = true;

    public static void createIndices(String indicesName) {
        if (!enabled) {
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                    new HttpHost("localhost", 9200, "http")))) {
                CreateIndexRequest request = new CreateIndexRequest(indicesName);
                client.indices().create(request);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    public static void main(String[] args) {
        createIndices("shmcloud_1");
    }

    public static void addDocToES(Map<String, Object> jsonMap, String indicesName, String id) {
        if (!enabled) {
            return;
        }
        try {
            try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(
                    new HttpHost("localhost", 9200, "http")))) {
                IndexRequest indexRequest = new IndexRequest(indicesName, indicesName, id)
                        .source(jsonMap);
                client.index(indexRequest);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
        }

    }
}