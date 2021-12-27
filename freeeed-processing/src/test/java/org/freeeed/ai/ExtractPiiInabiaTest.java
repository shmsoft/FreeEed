/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class ExtractPiiInabiaTest {
    private String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";

    static private String token;
    @Before
    public void initObjects() {
        token = System.getenv("INABIA_TOKEN");
    }
    @Test
    public void testInabiaPii() {
        String API_URL = "https://inabia.ai:8000/extractPII";
        //String API_URL = "https://vp3xir2ce6.execute-api.us-west-2.amazonaws.com/extractPII";
        data = data.replaceAll("<br>", " ").trim();
        data = "{ \"text\":" + "\"" + data + "\"}";
        try {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");

            RequestBody body = RequestBody.create(mediaType, data);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .method("POST", body)
                    .addHeader("token", token)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            String jsonString = response.body().string();
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray jsonResponse = (JSONArray) jsonObject.get("response");
            JSONArray pii = (JSONArray) jsonResponse.get(1);
            for (int i = 0; i < pii.size(); ++i) {
                JSONObject piiElement = (JSONObject) pii.get(i);
                System.out.println(piiElement);
            }
        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);
        }
    }
    @Test
    public void testExtractPii() {
        ExtractPiiInabia extract = new ExtractPiiInabia(token);
        List<String> result = extract.extractPii(data);
        assertEquals(result.size(), 7);
    }
    @Test
    public void testExtractPiiMuchoData() {
        ExtractPiiInabia extract = new ExtractPiiInabia(token);
        String muchoData = data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                + ". " + data
                ;
        List<String> result = extract.extractPii(muchoData);
        assertEquals(result.size(), 7);
    }

    @Test
    public void testExtractPiiAsString() {
        ExtractPiiInabia extract = new ExtractPiiInabia(token);
        String result = extract.extractPiiAsString(data);
        System.out.println(result);
        assert(result.length() > 0);
    }
}