package org.freeeed.ai.inabia;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class InabiaClient {

    // Testing in the browser: https://inabia.ai:8000/docs#/default/PII_extractPII_post

    //private String apiURL = "https://inabia.ai:8000/extractPII";
    private String apiURL = "https://vp3xir2ce6.execute-api.us-west-2.amazonaws.com/extractPII";
    private final String token;
    private final String document;
    private int maxLength = 500;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private HashMap<String, String> piiInDoc = new HashMap<>();

    public InabiaClient(String document, String token) {
        this.token = token;
        this.document = document;
    }

    public InabiaClient(String document, String token, int maxLength) {
        this.token = token;
        this.document = document;
        this.maxLength = maxLength;
    }

    public InabiaClient(String document, String token, int maxLength, String apiURL) {
        this.apiURL = apiURL;
        this.token = token;
        this.document = document;
        this.maxLength = maxLength;
    }

    private void updateHashMap(JSONObject ojbToAddToHashMap) {
        ojbToAddToHashMap.remove("Confidence");
        String key = ojbToAddToHashMap.keySet().iterator().next();
        String value = ojbToAddToHashMap.getString(key);
        if (piiInDoc.get(key) != null) {
            value = piiInDoc.get(key) + ", " + value;
        }
        piiInDoc.put(key, value);
    }

    public HashMap getPII() throws IOException, InterruptedException {
        maxLength -= 50;
        DocumentToSentences sentencesObj = new DocumentToSentences(document, maxLength);
        List<String> sentences = sentencesObj.getSentences();

        for (String sentence : sentences) {
            JSONObject sendingObject = new JSONObject();
            sendingObject.put("text", sentence);

            Request request = new Request.Builder()
                    .url(apiURL)
                    .header("token", token)
                    .post(RequestBody.create(sendingObject.toString(), JSON))
                    .build();

            Response serverResponse = InabiaOkHttpClient.getInstance().getClient().newCall(request).execute();

            try {
                JSONArray piiResponse = new JSONArray(serverResponse.body().string());
                for (int i = 0; i < piiResponse.length(); i++) {
                    updateHashMap(piiResponse.getJSONObject(i));
                }
            } catch (Exception x) {

            }
            serverResponse.close();
        }

        return piiInDoc;
    }


}
