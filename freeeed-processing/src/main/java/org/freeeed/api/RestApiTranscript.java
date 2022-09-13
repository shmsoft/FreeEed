package org.freeeed.api;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class RestApiTranscript {

    public static void main(String[] args) throws Exception {
        Transcript transcript = new Transcript();
        transcript.setAudio_url("https://bit.ly/3yxKEIY");
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);
        System.out.println(jsonRequest);
        String assemblyAiToken = System.getenv("ASSEMBLYAI_APU_KEY");
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", assemblyAiToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse <String> postResponse = client.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse.body());
    }
}
