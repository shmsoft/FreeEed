package org.freeeed.services;
import org.freeeed.util.LogFactory;

import java.io.IOException;
import java.util.zip.ZipFile;

public class ZipServices {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ZipServices.class.getName());

    public long calculateNumberEntriesInZip(String zipFile) {
        long entryCount = 0;
        try (ZipFile zip = new ZipFile(zipFile)) {
            entryCount = zip.stream().count();
        } catch (IOException e) {
            LOGGER.severe("Could not open zip file: " + zipFile);
        }
        return entryCount;
    }
}
