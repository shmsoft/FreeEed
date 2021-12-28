/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author mark
 * For testing
aws comprehend detect-pii-entities \
--text "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010" \
--language-code en
 */
public class ExtractPiiAwsTest {
    @Test
    public void testInabiaPii() {
        System.out.println("ExtractPiiAwsTest");
        String response = new ExtractPiiAws().extractPiiAsString("It does not matter");
        System.out.println(response);
        assertTrue(response.length() > 0);
    }
}