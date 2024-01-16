package org.freeeed.services;
import org.json.JSONObject;
import org.json.JSONArray;

public class UtilJson {
    private String question;
    private String answer;
    private String[] sources;

    public static void main(String[] args) {
        String jsonString = "{\"question\":\"who are the people involved\",\"answer\":\"The people involved are Melissa Murphy, Sara Shackleton, Sheila Tweed, Susan Bailey, Rhonda L. Denton, and Nancy Pelosi.\",\"sources\":[\"UPI_00005\",\"UPI_00001\",\"UPI_00003\"]}";
        UtilJson utilJson = new UtilJson();
        utilJson.parseJson(jsonString);
        System.out.println("Question: " + utilJson.getQuestion());
        System.out.println("Answer: " + utilJson.getAnswer());
        System.out.println("Sources: " + utilJson.getSourcesAsString());

    }

    public void parseJson(String jsonString) {
        JSONObject obj = new JSONObject(jsonString);
        question = obj.getString("question");
        answer = obj.getString("answer");
        JSONArray sourcesJson = obj.getJSONArray("sources");
        sources = new String[sourcesJson.length()];
        for (int i = 0; i < sourcesJson.length(); i++) {
            sources[i] = sourcesJson.getString(i);
        }
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getSourcesAsString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sources.length; i++) {
            builder.append(sources[i]);
            if (i < sources.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}