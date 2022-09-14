/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.tika;

import org.freeeed.ai.SummarizeText;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class RestApiTikaTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiTikaTest.class);

    @Test
    public void testCallHelloTika() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        String result = restApiTika.callHelloTika();
        assertTrue(result != null && result.contains("This is Tika Server"));
    }
}