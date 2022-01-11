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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class ExtractPiiInabiaTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPiiInabiaTest.class);

    private String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";

    private String token;
    @Before
    public void initKeys() {
        token = System.getenv("INABIA_TOKEN");
    }
    @Test
    public void testInabiaPii() {
        String API_URL = "https://inabia.ai:8000/extractPII";
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
            }
        } catch (Exception e) {
            LOGGER.error("Exception in NetClientGet:- " + e);
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
        String muchoData = "\"***********\n" +
        "EDRM Enron Email Data Set has been produced in EML, PST and NSF format by ZL Technologies, Inc. This Data Set is licensed under a Creative Commons Attribution 3.0 United States License <http://creativecommons.org/licenses/by/3.0/us/> . To provide attribution, please cite to \"ZL Technologies, Inc. (http://www.zlti.com).\" ***********\"}";
        List<String> result = extract.extractPii(muchoData);
        // TODO should be fixed by Inabia
        assertEquals(result.size(), 0);
    }

    @Test
    public void testExtractPiiAsString() {
        ExtractPiiInabia extract = new ExtractPiiInabia(token);
        String result = extract.extractPiiAsString(data);
        assert(result.length() > 0);
    }
}