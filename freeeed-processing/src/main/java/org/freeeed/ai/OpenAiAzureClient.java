package org.freeeed.ai;/*
 *@created 23/04/2024- 08:16
 *@author neha
 */

import com.google.gson.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class OpenAiAzureClient {
    private final String azureEndpoint;
    private final String apiKey;
    private final String apiVersion;
    private final HttpClient httpClient;
    private final Gson gson;

    public OpenAiAzureClient(String azureEndpoint, String apiKey, String apiVersion) {
        this.azureEndpoint = azureEndpoint;
        this.apiKey = apiKey;
        this.apiVersion = apiVersion;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new GsonBuilder().create();
    }

    public String createChatCompletion(List<Map<String, String>> messages) throws Exception {
        String requestBody = gson.toJson(Map.of(
                "model", "gpt4-turbo",
                "messages", messages,
                "temperature", 0.7,
                "max_tokens", 800,
                "top_p", 0.95,
                "frequency_penalty", 0,
                "presence_penalty", 0
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(azureEndpoint + "/openai/deployments/gpt4-turbo/chat/completions?api-version=" + apiVersion))
                .header("Content-Type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    //    public static void main(String[] args) throws Exception {
    public static String sendContentAndQuestionToAzureOpenAI(String content, List<String> questions) {
        try {
            OpenAiAzureClient client = new OpenAiAzureClient(
                    "https://freeeed-openai.openai.azure.com",
                    "864281421ae44d7c98efb5723ef60599",
                    "2024-02-15-preview"
            );

            List<Map<String, String>> messageText = constructMessages(content, questions);

            String completionResponse = client.createChatCompletion(messageText);
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> jsonMap = new Gson().fromJson(completionResponse, type);

            // Extract content from choices array
            return extractContent(jsonMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Map<String, String>> constructMessages(String content, List<String> questions) {
        List<Map<String, String>> messages = new ArrayList<>();

        // Add the content as the first message
        messages.add(Map.of("role", "system", "content", "You are a researcher tasked with extracting data from the provided text. Please answer in the json format and do not include content outside the provided content. "+content));

        // Add each question as a separate message
        StringBuilder questionBuilder = new StringBuilder();
        for (String question : questions) {
            questionBuilder.append(question + " ");
        }
        messages.add(Map.of("role", "user", "content", questionBuilder.toString()));

        return messages;
    }

    private static String extractContent(Map<String, Object> jsonMap) {
        if (jsonMap.containsKey("choices")) {
            Object choicesObject = jsonMap.get("choices");
            if (choicesObject instanceof Iterable) {
                Iterable choicesIterable = (Iterable) choicesObject;
                for (Object choiceObject : choicesIterable) {
                    if (choiceObject instanceof Map) {
                        Map<String, Object> choiceMap = (Map<String, Object>) choiceObject;
                        if (choiceMap.containsKey("message")) {
                            Map<String, Object> messageMap = (Map<String, Object>) choiceMap.get("message");
                            if (messageMap.containsKey("content")) {
                                return (String) messageMap.get("content");
//                                System.out.println("Content: " + content);
                            }
                        }
                    }
                }
            }
        }
        return " ";
    }
}

