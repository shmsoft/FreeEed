package org.freeeed.api;

import java.net.URI;
import java.net.http.HttpRequest;

public class RestApiTranscript {
    public static void main(String[] args) throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("https://bit.ly/3yxKEIY"))
                .header("Authorization", "03f3e46eda8a46e0a00dc5a9a1dc6a3f")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

    }
}
