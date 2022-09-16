package org.freeeed.api.tika;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * Tika-server REST API implementation
 * https://cwiki.apache.org/confluence/display/TIKA/TikaServer
 */

public class RestApiTika {
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * curl -X GET http://localhost:9998/tika
     */
    public String helloTika() throws Exception {
        String url = "http://localhost:9998/tika";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String postTo(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String bowlingJson(String player1, String player2) {
        return "{'winCondition':'HIGH_SCORE',"
                + "'name':'Bowling',"
                + "'round':4,"
                + "'lastSaved':1367702411696,"
                + "'dateStarted':1367702378785,"
                + "'players':["
                + "{'name':'" + player1 + "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
                + "{'name':'" + player2 + "','history':[6,10,5,10,10],'color':-48060,'total':41}"
                + "]}";
    }
    /*
    Metadata Resource
    /meta
     */

    /**
     * curl -T price.xls http://localhost:9998/meta
     */
    public String getMetadata(String filename) throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:9998/meta"))
                .PUT(HttpRequest.BodyPublishers.ofString(filename))
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> postResponse = client.send(postRequest, BodyHandlers.ofString());
        System.out.println(postResponse.body());
        return postResponse.body();
    }

    public String getText(String filename) throws Exception {
        URL url = new URL("http://localhost:9998/tika");
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("PUT");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "text/csv");
        http.setRequestProperty("Accept", "text/plain");

        byte[] data = FileUtils.readFileToByteArray(new File(filename));
        OutputStream stream = http.getOutputStream();
        stream.write(data);
        String response = stream.toString();
        http.disconnect();
        return response;
    }
}
