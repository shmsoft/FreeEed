package org.freeeed.api.transcribe;

import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import okhttp3.*;
import org.freeeed.services.Settings;
import org.freeeed.ui.ProjectUI;
import org.freeeed.util.LogFactory;

import java.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * REST API code and text-to-speech courtesy of https://www.youtube.com/watch?v=9oq7Y8n1t00
 */

public class RestApiTranscript {
    private final static Logger LOGGER = LogFactory.getLogger(ProjectUI.class.getName());

    public static void main(String[] args) {
        String url = "https://bit.ly/3yxKEIY";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.getTranscriptionFromUrl(url);
        System.out.println(transcript);
    }

    public String getTranscriptionFromFile(String filename) {
        String url = uploadToTheCloud(filename);
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.getTranscriptionFromUrl(url);
        System.out.println(transcript);
        return transcript;
    }

    public String uploadToTheCloud(String filename) {
        String url = "https://casify-public.s3.us-east-2.amazonaws.com/7510.mp3";
        return url;
    }

    public String getTranscriptionFromUrl(String url) {
        Transcript transcript = new Transcript();
        transcript.setAudio_url(url);
        Gson gson = new Gson();
        String jsonRequest = gson.toJson(transcript);
        System.out.println(jsonRequest);
        String assemblyAiToken = System.getenv("ASSEMBLYAI_API_KEY");
        try {
            assemblyAiToken = System.getenv("ASSEMBLYAI_API_KEY");

            HttpRequest postRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.assemblyai.com/v2/transcript"))
                    .header("Authorization", assemblyAiToken)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();
            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> postResponse = client.send(postRequest, BodyHandlers.ofString());
            System.out.println(postResponse.body());
            transcript = gson.fromJson(postResponse.body(), Transcript.class);
            System.out.println(transcript.getId());
            HttpRequest getRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.assemblyai.com/v2/transcript/" + transcript.getId()))
                    .header("Authorization", assemblyAiToken)
                    .build();
            while (true) {
                HttpResponse<String> getResponse = client.send(getRequest, BodyHandlers.ofString());
                transcript = gson.fromJson(getResponse.body(), Transcript.class);
                System.out.println(transcript.getStatus());
                if ("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                    break;
                }
                Thread.sleep(1000);
            }
            System.out.println("Transcription completed");
        } catch (Exception e) {
            System.out.println("Environment variable ASSEMBLYAI_API_KEY not set");
            System.exit(1);
        }
        return transcript.getText();
    }

    /**
     * Direct transcription from file probably should not be done. It may run into upload size limits.
     */

    public String transcribeDirectly(String filePath) throws IOException {
        String responseBody = "";
        OkHttpClient httpClient = new OkHttpClient();
        String apiKey = System.getenv("ASSEMBLYAI_API_KEY");
        MediaType mediaType = MediaType.parse("audio/mpeg"); // Change this according to your audio file type
        File audioFile = new File(filePath);
        RequestBody requestBody = RequestBody.create(audioFile, mediaType);

        Request request = new Request.Builder()
                .url("https://api.assemblyai.com/v2/transcript")
                .post(requestBody)
                .addHeader("authorization", apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Process the response here
            responseBody = response.body().string();
            // You can parse this response to get the transcription ID and then poll for the result
        }
        return responseBody;
    }

    public String transcribeWithFastAPI(String filePath) throws IOException {
        String apiURL = Settings.getSettings().getAiEndpoint() + "transcribe_audio/";
        File file = new File(filePath);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.MINUTES) // Set connect timeout
                .readTimeout(3, TimeUnit.MINUTES) // Set read timeout
                .build();
        Settings settings = Settings.getSettings();
        // Build a multipart/form-data body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("document", file.getName(),
                        RequestBody.create(file, MediaType.parse("audio/mpeg"))) // Replace with your file's MIME type
                .build();
        // Build the request
        Request request = new Request.Builder()
                .url(apiURL)
                .post(requestBody)
                .build();

        // Execute the request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Process and return the response body
            return response.body().string();
        }
    }
}
