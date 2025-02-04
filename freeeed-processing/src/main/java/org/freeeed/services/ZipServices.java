package org.freeeed.services;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ZipServices {
    private static ZipServices instance;
    private static final Object lock = new Object();
    public void addToJobSize(String zipFile) {
        long additionalSize = CalculateNumberEntriesInZip(zipFile);

    }
    private ZipServices() {}
    public long CalculateNumberEntriesInZip(String zipFile) {
        long entryCount = 0;
        try (ZipFile zip = new ZipFile(zipFile)) {
            entryCount = zip.stream().count();
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
        return entryCount;
    }
    public static ZipServices getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ZipServices();
                }
            }
        }
        return instance;
    }
}
