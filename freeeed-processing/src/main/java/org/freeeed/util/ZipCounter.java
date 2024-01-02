package org.freeeed.util;

import org.freeeed.ui.ProjectUI;

import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.FileInputStream;
import java.io.IOException;

public class ZipCounter {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ProjectUI.class.getName());
    public int numberElementsInZip(String zipFilePath) {
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("text/")) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}
