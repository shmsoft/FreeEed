package org.freeeed.ai;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing in the browser: http://18.218.29.151:8000/docs#
 */

public class SummarizeText {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeText.class);
    static String API_URL = "http://18.218.29.151:8000/docs#"; // TODO - the real URL?

    public String summarizeText(String fullText) {
        String mtext = fullText.replaceAll("<br>", " ").trim();
        mtext = new AIUtil().removeBreakingCharacters(mtext);
        mtext = "{ \"text\":" + "\"" + mtext + "\"}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, mtext);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
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
                    // TODO ???
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NetClientGet:- " + e);
        }

        // TODO summarize!
        return mtext;
    }
}
