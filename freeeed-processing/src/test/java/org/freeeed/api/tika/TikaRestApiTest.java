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
        String text = tikaRestApi.getText(file, Project.getCurrentProject().isOcrEnabled());
        assertNotNull(text);
        assertTrue(text.contains("ommission staff and other government officials have investigated\n" +
                "numerous compiaints from homeowners"));
        assertTrue(text.contains("TWIST-ON\n" +
                "PRESSURE WIRE\n" +
                "CONNECTOR"));
    }
}