/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;

import de.schlichtherle.truezip.file.TArchiveDetector;
import de.schlichtherle.truezip.file.TConfig;
import de.schlichtherle.truezip.file.TFile;
import de.schlichtherle.truezip.file.TFileInputStream;
import de.schlichtherle.truezip.fs.archive.zip.JarDriver;
import de.schlichtherle.truezip.socket.sl.IOPoolLocator;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.ui.ProcessProgressUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process zip files during Hadoop map step
 *
 * @author mark
 */
public class ZipFileProcessor extends FileProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ZipFileProcessor.class);
    private static final int TRUE_ZIP = 1;
    private static final int ZIP_STREAM = 2;
    private final int zipLibrary = TRUE_ZIP;
    static private final int BUFFER = 4096;
    private final byte data[] = new byte[BUFFER];

    /**
     * Constructor
     *
     * @param zipFileName Path to the file
     * @param metadataWriter
     * @param luceneIndex
     */
    public ZipFileProcessor(String zipFileName, MetadataWriter metadataWriter, 
            LuceneIndex luceneIndex) {
        super(metadataWriter, luceneIndex);
        this.zipFileName = zipFileName;
        TFile.setDefaultArchiveDetector(new TArchiveDetector("zip"));

        TConfig.get().setArchiveDetector(
                new TArchiveDetector(
                        "zip",
                        new JarDriver(IOPoolLocator.SINGLETON)));
    }

    /**
     * Unpack zip file, cull, emit map with responsive files
     *
     * @param isAttachment
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process(boolean isAttachment, MD5Hash hash) throws IOException, InterruptedException {
        switch (zipLibrary) {
            case TRUE_ZIP:
                logger.debug("Processing with TrueZip");
                processWithTrueZip(isAttachment, hash);
                break;
            case ZIP_STREAM:
                logger.debug("Processing with JavaZip");
                processWithZipStream();
                break;
        }
    }

    public static boolean isZip(String fileName) {
        logger.trace("Determine isPST for file {}", fileName);
        boolean isZip = false;
        String ext = Util.getExtension(fileName);
        if ("zip".equalsIgnoreCase(ext)) {
            isZip = true;
        }
        return isZip;
    }

    private void processWithZipStream()
            throws IOException, InterruptedException {
        try (FileInputStream fileInputStream = new FileInputStream(getZipFileName()); 
                ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream))) {

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
        }
    }

    /**
     * Uncompress zip file then process according to file format
     *
     * @param isAttachment is this an attachment or loose file
     * @param hash hash of the parent
     * @throws IOException
     * @throws InterruptedException
     */
    public void processWithTrueZip(boolean isAttachment, MD5Hash hash)
            throws IOException, InterruptedException {
        Project project = Project.getCurrentProject();
        // now we pass custodian name through the zip file comments
        // remove this line later on
//        project.setupCurrentCustodianFromFilename(getZipFileName());

        TFile tfile = new TFile(getZipFileName());
        try {
            processArchivesRecursively(tfile, isAttachment, hash);
        } catch (IOException | InterruptedException e) {
            Metadata metadata = new Metadata();
            logger.error("Error in staging", e);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
            emitAsMap(getZipFileName(), metadata);
        }
        TFile.umount(true);
        if (Project.getCurrentProject().isEnvHadoop()) {
            new File(getZipFileName()).delete();
        }
    }

    private void processArchivesRecursively(TFile tfile, boolean isAttachment, MD5Hash hash)
            throws IOException, InterruptedException {
        // Take care of special cases
        // TODO do better archive handling
        // tfile = treatAsNonArchive(tfile);
        if ((tfile.isDirectory() || tfile.isArchive())) {
            TFile[] files = tfile.listFiles();
            if (files != null) {
                for (TFile file : files) {
                    processArchivesRecursively(file, isAttachment, hash);
                }
            }
        } else {
            try {
                updateProgressUI();
                String tempFile = writeTrueZipEntry(tfile);
                // hack
                // TODO - deal with unwanted archiving
                if (!(new File(tempFile).exists())) {
                    logger.warn("Unwanted archive level skipped: " + tempFile);
                    return;
                }

                if (PstProcessor.isPST(tempFile)) {
                    new PstProcessor(tempFile, metadataWriter, getLuceneIndex()).process();
//                } else if (NSFProcessor.isNSF(tempFile)) {
//                    new NSFProcessor(tempFile, metadataWriter, getLuceneIndex()).process();
                } else {
                    String originalFileName = tfile.getPath();

                    if (originalFileName.startsWith(getZipFileName())) {
                        originalFileName = originalFileName.substring(getZipFileName().length() + 1);
                    }
                    // TODO need to get comment from tfile
                    DiscoveryFile discoveryFile = new DiscoveryFile(tempFile, originalFileName, isAttachment, hash);
                    discoveryFile.setCustodian("Need custodian!");
                    processFileEntry(discoveryFile);
                }
            } catch (Exception e) {
                logger.error("Problem processing zip file: ", e);

                Metadata metadata = new Metadata();
                metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
                emitAsMap(getZipFileName(), metadata);
            }
        }
    }
        private void updateProgressUI() {
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.updateProgress(Stats.getInstance().getCurrentItemCount());
        }
    }

    private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException, Exception {
        // uncompress and write to temporary file
        String tempFile = writeZipEntry(zipInputStream, zipEntry);
        if (PstProcessor.isPST(tempFile)) {
            new PstProcessor(tempFile, metadataWriter, getLuceneIndex()).process();
//        } else if (NSFProcessor.isNSF(tempFile)) {
//            new NSFProcessor(tempFile, metadataWriter, getLuceneIndex()).process();
        } else {
            processFileEntry(new DiscoveryFile(tempFile, zipEntry.getName()));
        }
    }

    /**
     *
     * @param tfile
     * @return
     * @throws IOException
     */
    private String writeTrueZipEntry(TFile tfile)
            throws IOException {
        TFileInputStream fileInputStream = null;
        String tempFileName = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            logger.trace("Extracting file: {}", tfile.getName());
            fileInputStream = new TFileInputStream(tfile);
            Metadata metadata = new Metadata();
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, tfile.getName());
            int count;

            String tmpDir = Settings.getSettings().getTmpDir();
            new File(tmpDir).mkdirs();
            tempFileName = tmpDir + createTempFileName(tfile.getName());
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
        logger.trace("Extracted to {}, size = {}", tempFileName, new File(tempFileName).length());
        return tempFileName;
    }

    private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
        logger.trace("Extracting: {}", zipEntry);

        // start collecting metadata
        Metadata metadata = new Metadata();
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.toString());

        // write the extracted file to disk
        int count;
        String tempFileName = Settings.getSettings().getTmpDir() + createTempFileName(zipEntry.getName());
        FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
        try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER)) {
            while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
                bufferedOutputStream.write(data, 0, count);
            }
            bufferedOutputStream.flush();
        }

        logger.trace("Extracted to {}, size = {}", tempFileName, new File(tempFileName).length());
        return tempFileName;
    }

    /**
     * Create temp filename on disk used to hold uncompressed zipped file data
     *
     * @param fileName
     * @return
     */
    private String createTempFileName(String fileName) {
        return "temp." +  Util.getExtension(fileName);
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
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        // TODO is this ever called?
        logger.trace("fileName = {}, metadata = {}", fileName, metadata.toString());
        MapWritable mapWritable = createMapWritable(metadata);
        //MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        metadataWriter.processMap(mapWritable);
        // update stats
        Stats.getInstance().increaseItemCount();
    }

    @Override
    String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        return discoveryFile.getRealFileName();
    }
}
