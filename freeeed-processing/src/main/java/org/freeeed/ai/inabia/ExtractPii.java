package org.freeeed.ai.inabia;

import okhttp3.*;
import org.freeeed.LoadDiscovery.DATProcessor;
import org.freeeed.services.Project;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExtractPii {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPii.class);
    private String token;
    public ExtractPii(String token) {
        this.token = token;
    }
    static String API_URL = "https://inabia.ai:8000/extractPII";
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
            JSONArray pii = (JSONArray) jsonResponse.get(1);
            for (int i = 0; i < pii.size(); ++i) {
                JSONObject piiElement = (JSONObject) pii.get(i);
                list.add(piiElement.toString());
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NetClientGet:- " + e);
        }
        return list;
    }
    public String extractPiiAsString(String data) {
        List <String> list = extractPii(data);
        StringBuffer buffer = new StringBuffer();
        for (String pii : list) {
            buffer.append(pii + "\n");
        }
        return buffer.toString();
    }
}
