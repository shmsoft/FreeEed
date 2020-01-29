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
package org.freeeed.mr;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.tika.metadata.Metadata;
import org.freeeed.main.*;
import org.freeeed.metadata.ColumnMetadata;
import org.freeeed.main.DiscoveryFile;
import org.freeeed.services.Project;
import org.freeeed.services.ProcessingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class MetadataWriter {

    Project project = Project.getCurrentProject();
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataWriter.class);
    private static volatile MetadataWriter mInstance;
    protected ColumnMetadata columnMetadata;
    private String metadataFileName;
    private File metadataFile;
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();
    protected int masterOutputFileCount;
    protected String outputKey;
    protected boolean isDuplicate;

    String tmpFolder = project.getResultsDir() + "\\tmp\\";

    private HashMap<DiscoveryFile, String> exceptionList = new HashMap<>();

    private HashMap<DiscoveryFile, String> nativeList = new HashMap<>();

    private MetadataWriter() {
    }

    public static MetadataWriter getInstance() {
        if (mInstance == null) {
            synchronized (MetadataWriter.class) {
                if (mInstance == null) {
                    mInstance = new MetadataWriter();
                }
            }
        }
        return mInstance;
    }

    public void processMap(MapWritable value, DiscoveryFile discoveryFile) throws IOException {

        columnMetadata.reinit();

        DocumentMetadata allMetadata = getAllMetadata(value);

        Metadata standardMetadata = getStandardMetadata(allMetadata);
        columnMetadata.addMetadata(standardMetadata);
        columnMetadata.addMetadata(allMetadata);

        // TODO deal with attachments
        if (allMetadata.hasParent()) {
            columnMetadata.addMetadataValue(DocumentMetadataKeys.ATTACHMENT_PARENT,
                    ParameterProcessing.UPIFormat.format(masterOutputFileCount));
        }

        String originalFileName = new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
        // add the text to the text folder
        String documentText = allMetadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        String textEntryName = ParameterProcessing.TEXT + "\\" + allMetadata.getUniqueId() + "_" + originalFileName + ".txt";
        if (documentText != null && documentText.length() > 0) {
            String tepmFolder = project.getResultsDir() + "\\tmp\\" + textEntryName;
            File f = new File(tepmFolder);
            f.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tepmFolder));
            writer.write(documentText);
            writer.close();
        }

        columnMetadata.addMetadataValue(DocumentMetadata.TEXT_LINK(), textEntryName);

        nativeList.put(discoveryFile, ParameterProcessing.NATIVE + "\\" + allMetadata.getUniqueId() + "_" + originalFileName);

        if (allMetadata.get(DocumentMetadataKeys.PROCESSING_EXCEPTION) != null) {
            exceptionList.put(discoveryFile, "exception\\" + allMetadata.getUniqueId() + "_" + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName());

        }

/*
        //TODO: Don't make pdf of a file that is already pdf!
        // add the pdf made from native to the PDF folder
        String pdfNativeEntryName = ParameterProcessing.PDF_FOLDER + "\\"
                + allMetadata.getUniqueId() + "_"
                + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                + ".pdf";
        BytesWritable pdfBytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE_AS_PDF));
        if (pdfBytesWritable != null) {
            String tepmFolder = project.getResultsDir() + "\\tmp\\" + pdfNativeEntryName;
            File f = new File(tepmFolder);
            f.getParentFile().mkdirs();
            FileUtils.writeByteArrayToFile(f, pdfBytesWritable.getBytes());
        }

        BytesWritable htmlBytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE_AS_HTML_NAME));
        if (htmlBytesWritable != null && false) {
            processHtmlContent(value, allMetadata, allMetadata.getUniqueId(), htmlBytesWritable);
        }
*/

        ProcessingStats.getInstance().increaseItemCount(discoveryFile.getPath().length());

        FreeEedMR.reduceFileToProcess(discoveryFile.getRealFileName());

        appendMetadata(columnMetadata.delimiterSeparatedValues());
        // prepare for the next file with the same key, if there is any
    }

    public void packNative() {
        ProcessingStats.getInstance().setTotalNative(nativeList.size());
        nativeList.forEach((v, k) -> {
            try {
                System.out.println(tmpFolder + k);
                File f = new File(tmpFolder + k);
                f.getParentFile().mkdirs();
                FileUtils.copyFile(v.getPath(), f);
                columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_NATIVE, k);
                ProcessingStats.getInstance().addDoneNative();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void packException() {
        ProcessingStats.getInstance().setTotalNative(exceptionList.size());
        exceptionList.forEach((v, k) -> {
            try {
                System.out.println(tmpFolder + k);
                File f = new File(tmpFolder + k);
                f.getParentFile().mkdirs();
                FileUtils.copyFile(v.getPath(), f);
                columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_EXCEPTION, k);
                ProcessingStats.getInstance().addDoneException();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void appendMetadata(String string) throws IOException {
        Files.append(string + ParameterProcessing.NL,
                metadataFile, Charset.defaultCharset());
    }

    private void processHtmlContent(MapWritable value, Metadata allMetadata, String uniqueId, BytesWritable htmlBytesWritable) throws IOException {

        if (htmlBytesWritable != null) {
            String htmlNativeEntryName = ParameterProcessing.HTML_FOLDER + "/"
                    + uniqueId + "_"
                    + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                    + ".html";
            zipFileWriter.addBinaryFile(htmlNativeEntryName, htmlBytesWritable.getBytes(), htmlBytesWritable.getLength());
            LOGGER.trace("Processing file: {}", htmlNativeEntryName);

            // get the list with other files part of the html output
            Text htmlFiles = (Text) value.get(new Text(ParameterProcessing.NATIVE_AS_HTML));
            if (htmlFiles != null) {
                String fileNames = htmlFiles.toString();
                String[] fileNamesArr = fileNames.split(",");
                for (String fileName : fileNamesArr) {
                    String entry = ParameterProcessing.HTML_FOLDER + "/" + fileName;

                    BytesWritable imageBytesWritable = (BytesWritable) value.get(
                            new Text(ParameterProcessing.NATIVE_AS_HTML + "/" + fileName));
                    if (imageBytesWritable != null) {
                        zipFileWriter.addBinaryFile(entry, imageBytesWritable.getBytes(), imageBytesWritable.getLength());
                        LOGGER.trace("Processing file: {}", entry);
                    }
                }
            }
        }
    }

    public void setup() throws IOException {
        Project project = Project.getCurrentProject();
        columnMetadata = new ColumnMetadata();
        columnMetadata.setFieldSeparator(String.valueOf(Delimiter.getDelim(project.getFieldSeparator())));
        columnMetadata.setAllMetadata(project.getMetadataCollect());
        // write standard metadata fields
        prepareMetadataFile();
        appendMetadata(columnMetadata.delimiterSeparatedHeaders());
    }

    private void prepareMetadataFile() {
        String rootDir;
        String custodian = Project.getCurrentProject().getCurrentCustodian();
//        String custodianExt = custodian.trim().length() > 0 ? "_" + custodian : "";
        rootDir = Project.getCurrentProject().getResultsDir();
        metadataFileName =
                rootDir
                        + System.getProperty("file.separator")
                        + Project.METADATA_FILE_NAME
//                    + custodianExt + ".csv";
                        + ".csv";
        new File(rootDir).mkdir();
        metadataFile = new File(metadataFileName);
        LOGGER.debug("Filename: {}", metadataFileName);
    }

    /**
     *
     */
    // TODO is this needed at all?
    private DocumentMetadata getStandardMetadata(Metadata allMetadata) {
        DocumentMetadata metadata = new DocumentMetadata();
        String documentOriginalPath = allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH);
        metadata.set("File Name", new File(documentOriginalPath).getName());
        return metadata;
    }

    private DocumentMetadata getAllMetadata(MapWritable map) {
        DocumentMetadata metadata = new DocumentMetadata();
        Set<Writable> set = map.keySet();
        Iterator<Writable> iter = set.iterator();
        while (iter.hasNext()) {
            String name = iter.next().toString();
            if (!ParameterProcessing.NATIVE.equals(name)
                    && !ParameterProcessing.NATIVE_AS_PDF.equals(name)
                    && !name.startsWith(ParameterProcessing.NATIVE_AS_HTML)) { // all metadata but native - which is bytes!
                Text value = (Text) map.get(new Text(name));
                metadata.set(name, value.toString());
            }
        }
        return metadata;
    }
}
