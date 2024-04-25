/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.tika;

import org.freeeed.main.FreeEedMain;
import org.freeeed.services.Project;
import org.freeeed.util.LogFactory;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class RestApiTikaTest {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(RestApiTikaTest.class.getName());

    @Test
    public void testHelloTika() throws Exception {
        TikaRestApi restApiTika = new TikaRestApi();
        String result = restApiTika.helloTika();
        assertTrue(result != null && result.contains("This is Tika Server"));
    }


    @Test
    public void testGetLanguage() throws Exception {
        TikaRestApi restApiTika = new TikaRestApi();
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
        TikaRestApi restApiTika = new TikaRestApi();
        File file = new File("test-data/02-loose-files/docs/spreadsheet/tti.xls");
        assertTrue(file.exists());
        HashMap<String, String> response = restApiTika.getMetadata(file);
        assertTrue(response.get("dc:title").contains("Delegation for Contract Administration - Texas Transportation Institute"));
    }


    @Test
    public void testGetText() throws Exception {
        TikaRestApi restApiTika = new TikaRestApi();
        File file = new File("test-data/02-loose-files/docs/spreadsheet/tti.xls");
        assertTrue(file.exists());
        String response = restApiTika.getText(file, Project.getCurrentProject().isOcrEnabled()).toLowerCase();
        assertTrue(response.contains("Delegation for Contract Administration"));
        assertTrue(response.contains("Texas Transportation Institute"));
    }
    @Test
    public void testStress() throws Exception {
        int numberTests = 100;
        for (int i = 0; i < numberTests; i++) {
            TikaRestApi restApiTika = new TikaRestApi();
            File file = new File("test-data/02-loose-files/docs/spreadsheet/tti.xls");
            String text = restApiTika.getText(file, Project.getCurrentProject().isOcrEnabled());
            assertTrue(text.contains("Delegation for Contract Administration"));
            assertTrue(text.contains("Texas Transportation Institute"));
            assertTrue(file.exists());
        }
    }
}