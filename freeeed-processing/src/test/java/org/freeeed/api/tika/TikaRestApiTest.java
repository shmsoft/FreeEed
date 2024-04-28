package org.freeeed.api.tika;

import junit.framework.TestCase;
import org.freeeed.services.Project;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 *@created 12/04/2024- 13:53
 *@author neha
 */public class TikaRestApiTest extends TestCase {

    public void testGetText() throws Exception {
        String fileName = "aluminum.pdf";
        ClassLoader classLoader = getClass().getClassLoader();
        assertNotNull(classLoader.getResource(fileName));

        Path resourcePath = Paths.get(classLoader.getResource(fileName).toURI());

        File file = resourcePath.toFile();
        TikaRestApi tikaRestApi = new TikaRestApi();
        String text = tikaRestApi.getText(file, true);
        assertNotNull(text);
        assertTrue(text.contains("Repairing"));
        assertTrue(text.contains("Aluminum"));
    }
}