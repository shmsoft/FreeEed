package org.freeeed.ai;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Testing in the browser: http://52.14.40.92/docs#
 */

public class SummarizeText {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeText.class);
    // Summarizer models: https://huggingface.co/models?sort=downloads&search=pegasus
    // Summarizer models table
    // structure: Google name, display name
    public static String[][] models = {
            {"google/pegasus-xsum", "News"},
            {"google/bigbird-pegasus-large-arxiv", "Long documents"},
            {"google/pegasus-cnn_dailymail", "Email",},
            {"google/pegasus-newsroom", "News important point",},
            {"google/pegasus-pubmed", "Medical long sentences",},
            {"google/pegasus-wikihow", "wiki",},
            {"google/pegasus-reddit_tifu", "reddit",},
            {"google/pegasus-billsum", "Another news",},
            {"google/roberta2roberta_L-24_cnn_daily_mail", "Another email",},
            {"nlpaueb/legal-bert-base-uncased", "Legal",}
    };

    static String API_URL = "http://52.14.40.92/summarizeText/";

    public String summarizeText(String fullText) {
        LOGGER.debug("Summarizing text");
        String summary = "";
        String mtext = fullText.replaceAll("<br>", " ").trim();
        mtext = new AIUtil().removeBreakingCharacters(mtext);
        mtext = "{ \"text\":" + "\"" + mtext + "\"}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().
                    readTimeout(60, TimeUnit.SECONDS)
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
            JSONArray jsonResponse = (JSONArray) jsonObject.get("summary");
            if (jsonResponse != null) {
                summary = (String) jsonResponse.get(0);
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NetClientGet:- " + e);
        }
        return summary;
    }
}
