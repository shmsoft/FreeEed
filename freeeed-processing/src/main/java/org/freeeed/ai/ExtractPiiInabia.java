package org.freeeed.ai;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Testing in the browser: https://inabia.ai:8000/docs#/default/PII_extractPII_post
 */

public class ExtractPiiInabia {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPiiInabia.class);
    private String token;
    public ExtractPiiInabia(String token) {
        this.token = token;
    }
    static String API_URL = "https://inabia.ai:8000/extractPII";
    private int charLimit = 4000;

    public List<String> extractPii(String data) {
        List<String> list = new ArrayList<>();
        data = data.replaceAll("<br>", " ").trim();
        data = "{ \"text\":" + "\"" + data + "\"}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, data);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
                    .addHeader("token", token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonResponse = (JSONArray) jsonObject.get("response");
            if (jsonResponse != null) {
                JSONArray pii = (JSONArray) jsonResponse.get(1);
                for (int i = 0; i < pii.size(); ++i) {
                    JSONObject piiElement = (JSONObject) pii.get(i);
                    list.add(piiElement.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NetClientGet:- " + e);
        }
        return list;
    }
    public String extractPiiAsString(String data) {
        List <String> list = extractPIIBySegment(data);
        StringBuffer buffer = new StringBuffer();
        for (String pii : list) {
            buffer.append(pii + "\n");
        }
        return buffer.toString();
    }
    public List<String> extractPIIBySegment(String document) {
        List<String> accumulator = new ArrayList<>();
        TextSplitter splitter = new TextSplitter(charLimit);

        List<String> segments = splitter.splitBySentenceWithLimit(document);

        for (String segment: segments) {
            List<String> pii = extractPii(segment);
            accumulator.addAll(pii);
        }
        return accumulator;
    }
}

