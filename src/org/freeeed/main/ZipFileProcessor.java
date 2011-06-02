package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.tika.metadata.Metadata;

public class ZipFileProcessor extends FileProcessor {
    static private final int BUFFER = 4096;
    
    public ZipFileProcessor(String zipFileName, Context context) {
        super(context);
        setZipFileName(zipFileName);
    }
    @Override
    public void process()
            throws IOException {
        // unpack the zip file
        FileInputStream fileInputStream = new FileInputStream(getZipFileName());
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
                processZipEntry(zipInputStream, zipEntry);
            } catch (InterruptedException e) {
                // TODO - add better error handling
                e.printStackTrace(System.out);
            }
        }
        zipInputStream.close();
    }
    private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException, InterruptedException {
        // write the file
        String tempFile = writeZipEntry(zipInputStream, zipEntry);        
        if (PstProcessor.isPST(tempFile)) {
            new PstProcessor(tempFile, getContext()).process();
        } else {
            processFileEntry(tempFile, zipEntry.getName());
        }
    }    
    private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        System.out.println("Extracting: " + zipEntry);
        Metadata metadata = new Metadata();
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.toString());
        int count;
        byte data[] = new byte[BUFFER];
        // write the file to the disk
        String tempFileName = "/tmp/" + createTempFileName(zipEntry);
        FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
            bufferedOutputStream.write(data, 0, count);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        return tempFileName;
    }    
    private String createTempFileName(ZipEntry zipEntry) {
        String fileName = "temp." + Util.getExtension(zipEntry.getName());
        return fileName;
    }
    
}
