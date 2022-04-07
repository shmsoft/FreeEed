/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.piranha;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class PreProcessorTest {
    @Test
    public void testAddToInventory() throws IOException {
        String sourceDirectoryName = "test-data/01-one-time-test";
        new File("output").mkdirs();
        String flatInventoryFileName = "output/flatinventory.csv";
        PreProcessor preProcessor = new PreProcessor(sourceDirectoryName, flatInventoryFileName);
        preProcessor.addToInventory();
        assertTrue(new File(flatInventoryFileName).length() > 0);
    }
}