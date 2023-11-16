package org.freeeed.ai;

import okhttp3.*;
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
    // structure: Google codename, display name
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
            {"nsi319/legal-pegasus", "Legal",}
    };

    static String API_URL = "http://52.14.40.92/summarizeTextModel/";

    public String summarizeText(String fullText) {
        return summarizeText(fullText, "");
    }

    public String summarizeText(String fullText, String modelCodeName) {
        LOGGER.debug("Summarizing text with model: " + modelCodeName);
        String summary = "";
        String cleanText = fullText.replaceAll("<br>", " ").trim();
        cleanText = new AIUtil().removeBreakingCharacters(cleanText);
        String modelText = "";
        if (!modelCodeName.isEmpty()) {
            modelText = ",\"model\"" + ":" + "\"" + modelCodeName + "\"";
        } else {
            modelText = ",\"model\"" + ":" + "\"" + models[0][0] + "\"";
        }
        cleanText = "{ \"text\":" + "\"" + cleanText + "\"" +
                modelText +
                "}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().
                    readTimeout(60, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.get("application/json");
            RequestBody body = RequestBody.create(cleanText, mediaType);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            summary = getSummaryFromJson(jsonString);
        } catch (Exception e) {
            LOGGER.error("Error while summarizing text: " + e.getMessage());
        }
        return summary;
    }

    public static int detModelIndex(String modelCode) {
        for (int i = 0; i < models.length; ++i) {
            if (modelCode.equalsIgnoreCase(models[i][0])) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Get the summary from the json string
     * @param jsonString
     * @return summary
     * Reference for Jackson: https://www.baeldung.com/
     */
    private String getSummaryFromJson(String jsonString) {
        String summary = "";
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode root = mapper.readTree(jsonString);
//            JsonNode summaryNode = root.get("summary");
//            if (summaryNode != null && summaryNode.isArray()) {
//                summaryNode = summaryNode.get(0);
//                summary = summaryNode.asText();
//            }
//        } catch (Exception e) {
//            LOGGER.error("Error while parsing json: " + e.getMessage());
//        }
        return summary;
    }
}
