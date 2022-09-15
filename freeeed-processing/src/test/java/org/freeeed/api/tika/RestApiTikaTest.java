/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.tika;

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
    public void testHelloTika() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        String result = restApiTika.helloTika();
        assertTrue(result != null && result.contains("This is Tika Server"));
    }
    @Test
    public void getMetadata() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        String fileName = "test-data/02-loose-files/docs/spreadsheet$/tti.xls";
        String result = restApiTika.getMetadata(fileName);
        System.out.println(result);
        assertTrue(true);
    }
    @Test
    public void testGetText() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        String fileName = "test-data/02-loose-files/docs/spreadsheet$/tti.xls";
        String result = restApiTika.getText(fileName);
        System.out.println(result);
        assertTrue(true);
    }

}