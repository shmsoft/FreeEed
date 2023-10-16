package org.freeeed.api.transcribe;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * REST API code and text-to-speech courtesy of https://www.youtube.com/watch?v=9oq7Y8n1t00
 */

public class RestApiTranscript {

    public static void main(String[] args) throws Exception {
        System.out.println("Currently, this is not part of FreeEed and scheduled to be archived.");
        System.exit(9);
        Transcript transcript = new Transcript();
        transcript.setAudio_url("https://bit.ly/3yxKEIY");
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);
        System.out.println(jsonRequest);
        String assemblyAiToken = System.getenv("ASSEMBLYAI_API_KEY");
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", assemblyAiToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse <String> postResponse = client.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse.body());
        transcript = gson.fromJson(postResponse.body(), Transcript.class);
        System.out.println(transcript.getId());
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                .header("Authorization", assemblyAiToken)
                .build();
        while(true) {
            HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), Transcript.class);
            System.out.println(transcript.getStatus());
            if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                break;
            }
            Thread.sleep(1000);
        }
        System.out.println("Transcription completed");
        System.out.println(transcript.getText());
    }
}
