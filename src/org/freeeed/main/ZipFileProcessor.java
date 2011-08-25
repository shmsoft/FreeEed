package org.freeeed.main;

import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
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

/**
 * Process zip files during Hadoop map step
 * 
 * @author mark
 */
public class ZipFileProcessor extends FileProcessor {

    private static final int TRUE_ZIP = 1;
    private static final int ZIP_STREAM = 2;
    private int zipLibrary = TRUE_ZIP;
    static private final int BUFFER = 4096;

    /**
     * Constructor
     *
     * @param zipFileName Path to the file
     * @param context File context
     */
    public ZipFileProcessor(String zipFileName, Context context) {
        super(context);
        setZipFileName(zipFileName);
    }

    /**
     * Unpack zip file, search for query matches, add results to map
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process() throws IOException, InterruptedException {
        switch (zipLibrary) {
            case TRUE_ZIP:
                processWithTrueZip();
                break;
            case ZIP_STREAM:
                processWithZipStream();
                break;
        }
    }

    private void processWithZipStream()
            throws IOException, InterruptedException {
        // unpack the zip file
        FileInputStream fileInputStream = new FileInputStream(getZipFileName());
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
        
        // loop through each entry in the zip file
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            try {
                // process zip file and extract metadata using Tika
                processZipEntry(zipInputStream, zipEntry);
            } catch (Exception e) {
                // debug stack trace
                e.printStackTrace(System.out);

                // add exceptions to output
                Metadata metadata = new Metadata();
                metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
                emitAsMap(getZipFileName(), metadata);
            }
        }
        zipInputStream.close();
    }

    /**
     * Uncompress zip file then process according to file format
     *
     * @param zipInputStream
     * @param zipEntry
     * @throws IOException
     * @throws Exception
     */
    public void processWithTrueZip()
            throws IOException, InterruptedException {
        TFile tfile = new TFile(getZipFileName());
        try {
            processArchivesRecursively(tfile);
        } catch (Exception e) {
            Metadata metadata = new Metadata();
            e.printStackTrace(System.out);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
            emitAsMap(getZipFileName(), metadata);
        }
    }

    private void processArchivesRecursively(TFile tfile)
            throws IOException, InterruptedException {
        TFileInputStream fileInputStream = null;
        if (!tfile.isFile()) {
            TFile[] files = tfile.listFiles();
            for (TFile file : files) {
                processArchivesRecursively(file);
            }
        } else {
            fileInputStream = new TFileInputStream(tfile);
            try {
                String tempFile = writeTrueZipEntry(fileInputStream, tfile.getName());
                if (PstProcessor.isPST(tempFile)) {
                    new PstProcessor(tempFile, getContext()).process();
                } else {
                    processFileEntry(tempFile, tfile.getName());
                }
            } catch (Exception e) {
                Metadata metadata = new Metadata();
                e.printStackTrace(System.out);
                metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
                emitAsMap(getZipFileName(), metadata);
            }
        }
        // finally
        // close reader

    }

    private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException, Exception {
        // uncompress and write to temporary file
        String tempFile = writeZipEntry(zipInputStream, zipEntry);
        if (PstProcessor.isPST(tempFile)) {
            new PstProcessor(tempFile, getContext()).process();
        } else {
            processFileEntry(tempFile, zipEntry.getName());
        }
    }

    /**
     * Uncompress and write zip data to file in /tmp directory
     *
     * @param zipInputStream
     * @param zipEntry
     * @return
     * @throws IOException
     */
    private String writeTrueZipEntry(TFileInputStream fileInputStream, String fileName)
            throws IOException {
        History.appendToHistory("Extracting: " + fileName);
        Metadata metadata = new Metadata();
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, fileName);
        int count;
        byte data[] = new byte[BUFFER];
        // write the file to the disk
        String tempFileName = "/tmp/" + createTempFileName(fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
        while ((count = fileInputStream.read(data, 0, BUFFER)) != -1) {
            bufferedOutputStream.write(data, 0, count);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        return tempFileName;
    }

    private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        // update application log
        History.appendToHistory("Extracting: " + zipEntry);

        // create Tike metadata
        Metadata metadata = new Metadata();
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.toString());

        // write the zip file to disk
        int count;
        byte data[] = new byte[BUFFER];
        // create temporary directory file path
        String tempFileName = "/tmp/" + createTempFileName(zipEntry.getName());
        // create file in temporary directory
        FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
        // read and uncompress zip contents into newly created file
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
            bufferedOutputStream.write(data, 0, count);
        }
        // close file
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        
        return tempFileName;
    }

    /**
     * Create temp filename on disk used to hold uncompressed zip file data
     *
     * @param zipEntry
     * @return
     */
    private String createTempFileName(String fileName) {
        String tempFileName = "temp." + Util.getExtension(fileName);
        return tempFileName;
    }

    /**
     * @return the zipLibrary
     */
        
    public int getZipLibrary() {
        return zipLibrary;
    }

    /**
     * Create a map
     *
     * @param metadata Tika class of key/value pairs to place in map
     * @param fileName ???
     * @return Map located on heap with key/value pairs added
     */
    private MapWritable createMapWritable(Metadata metadata, String fileName) {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        return mapWritable;
    }

    /**
     * Add the search result (Tika metadata) to Hadoop context
     *
     * @param fileName
     * @param metadata
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, fileName);
        MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        getContext().write(key, mapWritable);
    }
}
