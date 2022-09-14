package org.freeeed.api.tika;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * REST API code and text-to-speech courtesy of https://www.youtube.com/watch?v=9oq7Y8n1t00
 */

public class RestApiTika {

    public static void main(String[] args) throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        //restApiTika.requestResponse();
        String hello = restApiTika.callHelloTika();
        System.out.println(hello);
    }
    /**
    curl -X GET http://localhost:9998/tika
     */
    public String callHelloTika() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:9998/tika"))
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
        String returnString = getResponse.body();
        return returnString;
    }
    public void requestResponse() throws Exception {
        TikaTranscript transcript = new TikaTranscript();
        transcript.setAudio_url("https://bit.ly/3yxKEIY");
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);
        System.out.println(jsonRequest);
        String tikaToken = "";
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                .header("Authorization", tikaToken)
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse <String> postResponse = client.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse.body());
        transcript = gson.fromJson(postResponse.body(), TikaTranscript.class);
        System.out.println(transcript.getId());
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                .header("Authorization", tikaToken)
                .build();
        while(true) {
            HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
            transcript = gson.fromJson(getResponse.body(), TikaTranscript.class);
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
