/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai.inabia;

import com.google.gson.JsonObject;
import okhttp3.*;
import org.junit.Test;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class PiiTest {
    @Test
    public void testInabiaPii() {
        System.out.println("testInabiaPii");
        String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";
        String API_URL = "https://inabia.ai:8000/extractPII";
        //String API_URL = "https://vp3xir2ce6.execute-api.us-west-2.amazonaws.com/extractPII";
        data = data.replaceAll("<br>", " ").trim();
        data = "{ \"text\":" + "\"" + data + "\"}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            String token = System.getenv("INABIA_TOKEN");
            System.out.println(data);
            RequestBody body = RequestBody.create(mediaType, data);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
                    .addHeader("token", token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            System.out.println(jsonString);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonString);
            System.out.println(obj);
            JSONObject jsonObject = (JSONObject) obj;
            System.out.println(jsonObject);
            JSONArray jsonResponse = (JSONArray) jsonObject.get("response");
            System.out.println(jsonResponse);
            JSONArray pii = (JSONArray) jsonResponse.get(1);
            System.out.println(pii);
            for (int i = 0; i < pii.size(); ++i) {
                JSONObject piiElement = (JSONObject) pii.get(i);
                System.out.println(piiElement);
            }
        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);
        }
    }
}