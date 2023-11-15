/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.tika;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;

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
    public void testGetLanguage() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        String phrase = "comme çi comme ça";
        String response = restApiTika.getLanguage(phrase);
        assertEquals("fr", response);
        phrase = "Olá como vai você?";
        response = restApiTika.getLanguage(phrase);
        assertEquals("pt", response);
    }

    /**
     * curl -T freeeed-processing/test-data/02-loose-files/docs/spreadsheet/tti.xls http://localhost:9998/meta
     */

    @Test
    public void testGetMetadata() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        File file = new File("test-data/02-loose-files/docs/spreadsheet/tti.xls");
        assertTrue(file.exists());
        HashMap<String, String> response = restApiTika.getMetadata(file);
        assertTrue(response.get("dc:title").contains("Delegation for Contract Administration - Texas Transportation Institute"));
    }


    @Test
    public void testGetText() throws Exception {
        RestApiTika restApiTika = new RestApiTika();
        File file = new File("test-data/02-loose-files/docs/spreadsheet/tti.xls");
        assertTrue(file.exists());
        String response = restApiTika.getText(file);
        assertTrue(response.contains("Delegation for Contract Administration"));
        assertTrue(response.contains("Texas Transportation Institute"));
    }
}