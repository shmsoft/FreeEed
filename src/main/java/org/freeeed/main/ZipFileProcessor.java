package org.freeeed.main;

import de.schlichtherle.truezip.file.TArchiveDetector;
import org.freeeed.services.FreeEedUtil;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.tika.metadata.Metadata;
import org.freeeed.main.PlatformUtil.PLATFORM;
import org.freeeed.services.History;
import org.freeeed.services.Project;

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
        TFile.setDefaultArchiveDetector(new TArchiveDetector("zip"));
    }

    /**
     * Unpack zip file, cull, emit map with responsive files
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
        fileInputStream.close();
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
        TFile.umount(true);
        if (Project.getProject().isEnvHadoop()) {
            new File(getZipFileName()).delete();
        }
    }

    private void processArchivesRecursively(TFile tfile)
            throws IOException, InterruptedException {
        // Take care of special cases
        // TODO do better archive handling
        // tfile = treatAsNonArchive(tfile);
        if ((tfile.isDirectory() || tfile.isArchive())) {
            TFile[] files = tfile.listFiles();
            if (files != null) {
                for (TFile file : files) {
                    processArchivesRecursively(file);
                }
            }
        } else {
            try {
                String tempFile = writeTrueZipEntry(tfile);
                // hack
                // TODO - deal with unwanted archiving
                if (!(new File(tempFile).exists())) {
                    System.out.println("Warning: unwanted archive level skipped: " + tempFile);
                    return;
                }
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
     * Uncompress and write zip data to file
     *
     * @param zipInputStream
     * @param zipEntry
     * @return
     * @throws IOException
     */
    private String writeTrueZipEntry(TFile tfile)
            throws IOException {
        TFileInputStream fileInputStream = null;
        String tempFileName = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            History.appendToHistory("Extracting: " + tfile.getName());
            fileInputStream = new TFileInputStream(tfile);
            Metadata metadata = new Metadata();
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, tfile.getName());
            int count;
            byte data[] = new byte[BUFFER];
            new File(ParameterProcessing.TMP_DIR).mkdirs();
            tempFileName = ParameterProcessing.TMP_DIR + createTempFileName(tfile.getName());
            FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
            while ((count = fileInputStream.read(data, 0, BUFFER)) != -1) {
                bufferedOutputStream.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
            }
        }
        return tempFileName;
    }

    private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        History.appendToHistory("Extracting: " + zipEntry);

        // start collecting metadata
        Metadata metadata = new Metadata();
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.toString());

        // write the extracted file to disk
        int count;
        byte data[] = new byte[BUFFER];
        String tempFileName = ParameterProcessing.TMP_DIR + createTempFileName(zipEntry.getName());
        FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
        while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
            bufferedOutputStream.write(data, 0, count);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();

        return tempFileName;
    }

    /**
     * Create temp filename on disk used to hold uncompressed zipped file data
     *
     * @param zipEntry
     * @return
     */
    private String createTempFileName(String fileName) {
        String tempFileName = "temp." + FreeEedUtil.getExtension(fileName);
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
     * @return MapWritable with key/value pairs added
     */
    private MapWritable createMapWritable(Metadata metadata) {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            String value = metadata.get(name);
            // TODO how could value be null? (but it did happen to me)
            if (value == null) {
                value = "";
            }
            mapWritable.put(new Text(name), new Text(value));
        }
        return mapWritable;
    }

    /**
     * Emit the map with all metadata, native, and text
     *
     * @param fileName
     * @param metadata
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        if (Project.getProject().checkSkip()) {
            return;
        }
        //History.appendToHistory("emitAsMap: fileName = " + fileName + " metadata = " + metadata.toString());
        MapWritable mapWritable = createMapWritable(metadata);
        MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        if ((PlatformUtil.getPlatform() == PLATFORM.LINUX) || (PlatformUtil.getPlatform() == PLATFORM.MACOSX)) {
            getContext().write(key, mapWritable);
        } else if (PlatformUtil.getPlatform() == PLATFORM.WINDOWS) {
            List<MapWritable> values = new ArrayList<MapWritable>();
            values.add(mapWritable);
            WindowsReduce.getInstance().reduce(key, values, null);
        }
    }

    @Override
    String getOriginalDocumentPath(String tempFile, String originalFileName) {
        return originalFileName;
    }

    private TFile treatAsNonArchive(TFile tfile) {
        String ext = FreeEedUtil.getExtension(tfile.getName());
        if ("odt".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext)) {
            return new TFile(tfile.getParentFile(), tfile.getName(),
                    TArchiveDetector.NULL);
        } else {
            return tfile;
        }
    }
}
