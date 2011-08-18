package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.History;

public class ZipFileProcessor extends FileProcessor {

    static private final int BUFFER = 4096;

    public ZipFileProcessor(String zipFileName, Context context) {
        super(context);
        setZipFileName(zipFileName);
    }

    @Override
    public void process()
            throws IOException, InterruptedException {
        // unpack the zip file
        FileInputStream fileInputStream = new FileInputStream(getZipFileName());
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
                processZipEntry(zipInputStream, zipEntry);
            } catch (Exception e) {
                Metadata metadata = new Metadata();
                e.printStackTrace(System.out);
                metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
                emitAsMap(getZipFileName(), metadata);
            }
        }
        zipInputStream.close();
    }

    private MapWritable createMapWritable(Metadata metadata, String fileName) {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        return mapWritable;
    }

    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, fileName);
        MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        getContext().write(key, mapWritable);
    }

    private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException, Exception {
        // write the file
        String tempFile = writeZipEntry(zipInputStream, zipEntry);
        if (PstProcessor.isPST(tempFile)) {
            new PstProcessor(tempFile, getContext()).process();
        } else {
            processFileEntry(tempFile, zipEntry.getName());
        }
    }

    private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        History.appendToHistory("Extracting: " + zipEntry);
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
