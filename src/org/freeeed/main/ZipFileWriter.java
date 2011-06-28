package org.freeeed.main;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Opens a zip file for output and writes the text, native files, and exceptions 
 * into it.
 */
public class ZipFileWriter {

    private static final String rootDir = "test-output"
            + System.getProperty("file.separator") + "output";
    public static final String zipFileName = rootDir
            + System.getProperty("file.separator") + "output.zip";
    private ZipOutputStream zipOutputStream;
    private FileOutputStream fileOutputStream;

    public void openZipForWriting() throws IOException {
        fileOutputStream = new FileOutputStream(zipFileName);
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
    }

    public void closeZip() throws IOException {
        zipOutputStream.close();
        fileOutputStream.close();
    }

    public void addTextFile(String entryName, String textContent) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(zipEntry);
        if (textContent == null) {
            textContent = "No text extracted";
        }
        zipOutputStream.write(textContent.getBytes());
    }
    public void addBinaryFile(String entryName, byte[] fileContent, int length) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(fileContent, 0, length);
        
    }
}
