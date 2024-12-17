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

import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;
import org.freeeed.services.Util;
import org.freeeed.util.LogFactory;
import org.freeeed.util.MboxToEmlConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Process zip files
 *
 * @author mark
 */
public class FolderProcessor extends FileProcessor {

    private final static Logger LOGGER = LogFactory.getLogger(FolderProcessor.class.getName());
    static private final int BUFFER = 4096;
    private final byte data[] = new byte[BUFFER];

    /**
     * Constructor
     *
     * @param folderPath Path to the file
     * @param metadataWriter
     * @param luceneIndex
     */
    public FolderProcessor(String folderPath, MetadataWriter metadataWriter,
                           LuceneIndex luceneIndex) {
        super(metadataWriter, luceneIndex);
        this.zipFileName = folderPath;
    }

    /**
     * Unpack zip file, cull, emit map with responsive files
     *
     * @param isAttachment
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process(boolean isAttachment, String hash) throws IOException, InterruptedException {
        LOGGER.fine("Processing folder");
        processFolder();
    }


    public static Set<String> listFiles(String dir) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        }
    }
    private void processFolder()
            throws IOException, InterruptedException {

        Set<String> files = listFiles(this.zipFileName);
        for (String file : files) {
            try {
                // process zip file and extract metadata using Tika
                processFileEntry(file);
            } catch (Exception e) {
                // debug stack trace
                e.printStackTrace(System.out);
                // add exceptions to output
                Metadata metadata = new Metadata();
                metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, getZipFileName());
            }
        }
    }

    private void processFileEntry(String file) throws Exception {
        Path path = Paths.get(file);
        String fileName = path.getFileName().toString();
        if(file.endsWith(".mbox")){
            File outputDir = new File(Project.getCurrentProject().getMboxOutputDir());
            if(!outputDir.exists())
            {
                outputDir.mkdirs();
            }
            List<String> emailFiles = MboxToEmlConverter.convertMboxToEml(file, fileName, outputDir.getPath());
            for (String eml : emailFiles) {
                processSingleFile(eml, eml);
            }
        }else {
            processSingleFile(file, fileName);
        }
    }

    private void processSingleFile(String tempFile, String fileName) throws Exception {
        if (PstProcessor.isPST(tempFile)) {
            new PstProcessor(tempFile, metadataWriter, getLuceneIndex()).process();
        } else {
            processFileEntry(new DiscoveryFile(tempFile, fileName));
        }
    }

    /**
     * Create a map
     *
     * @param metadata Tika class of key/value pairs to place in map
     * @return MapWritable with key/value pairs added
     */
    private Map<String, String> createMapWritable(Metadata metadata) {
        Map<String, String> mapWritable = new HashMap<String, String>();
        String[] names = metadata.names();
        for (String name : names) {
            String value = metadata.get(name);
            // TODO how could value be null? (but it did happen to me)
            if (value == null) {
                value = "";
            }
            mapWritable.put(name, value);
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
        LOGGER.fine("fileName, metadata " +  fileName + metadata.toString());
        Map<String, String> mapWritable = createMapWritable(metadata);
        //MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        //metadataWriter.processMap(mapWritable);
        // update stats
        Stats.getInstance().increaseItemCount();
    }

    @Override
    String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        return discoveryFile.getPath().getAbsolutePath();
    }
}
