package org.freeeed.api.tika;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;

/**
 * Tika-server REST API implementation
 * https://cwiki.apache.org/confluence/display/TIKA/TikaServer
 */

public class RestApiTika {
    /**
    curl -X GET http://localhost:9998/tika
     */
    public String helloTika() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:9998/tika"))
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
        String returnString = getResponse.body();
        return returnString;
    }
    /*
    Metadata Resource
    /meta
     */
    /**
     curl -T price.xls http://localhost:9998/meta
     */
    public String getMetadata(String filename) throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:9998/meta"))
                .PUT(HttpRequest.BodyPublishers.ofString(filename))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse <String> postResponse = client.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse.body());
        return postResponse.body();
    }
    public String getText(String filename) throws Exception {
        URL url = new URL("http://localhost:9998/tika");
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "text/csv");
        http.setRequestProperty("Accept", "text/plain");

        String data = filename;

        byte[] out = data.getBytes(StandardCharsets.UTF_8);

        OutputStream stream = http.getOutputStream();
        //stream.write(out);
        String response = stream.toString();
        http.disconnect();
        return response;
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
