/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai.inabia;

import okhttp3.*;
import org.freeeed.services.Project;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class PiiTest {
    //@Test
    public void testGetPii() {
        System.out.println("testGetPii");
        String str = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";
        String inabia_token = System.getenv("INABIA_TOKEN");
        InabiaClient client = new InabiaClient(str,inabia_token,100);

        try {
            String expectedResults = "{Address=1301 Mckinney St #2400, Houston, Tx 77010, Phone=713-777-7777, Name=John Doe}";
            String returnResult = client.getPII().toString();
            assertEquals(expectedResults, returnResult);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testInabiaPii() {
        System.out.println("testInabiaPii");
        String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";
        //String API_URL = "https://inabia.ai:8000/extractPII";
        String API_URL = "https://vp3xir2ce6.execute-api.us-west-2.amazonaws.com/extractPII";

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
            String jsonData = response.body().string();

            System.out.println(jsonData);

        } catch (Exception e) {
            System.out.println("Exception in NetClientGet:- " + e);

        }
    }
}