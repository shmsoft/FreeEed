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
        assertEquals(result.size(), 2);
    }

    @Test
    public void testExtractPiiAsString() {
        ExtractPiiInabia extract = new ExtractPiiInabia(token);
        String result = extract.extractPiiAsString(data);
        assert(result.length() > 0);
    }
}