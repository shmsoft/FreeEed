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

import org.apache.commons.io.FileUtils;
import org.freeeed.data.index.ESIndex;
import org.freeeed.main.*;
import org.freeeed.metadata.ColumnMetadata;
import org.freeeed.main.DiscoveryFile;
import org.freeeed.services.Project;
import org.freeeed.services.ProcessingStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class MetadataWriter {

    private Project project;
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataWriter.class);
    private static volatile MetadataWriter mInstance;
    private ColumnMetadata columnMetadata;
    private File metadataFile;
    private ZipFileWriter zipFileWriter = new ZipFileWriter();
    private int masterOutputFileCount;
    protected String outputKey;
    protected boolean isDuplicate;
    private String tmpFolder;
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

    public synchronized void processMap(DiscoveryFile discoveryFile) throws IOException {
        columnMetadata.reinit();
        DocumentMetadata metadata = discoveryFile.getMetadata();
        columnMetadata.addMetadata(metadata);

        String originalFileName = new File(metadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
        String documentText = metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        String textEntryName = ParameterProcessing.TEXT + System.getProperty("file.separator") + metadata.getUniqueId() + "_" + originalFileName + ".txt";
        String nativeEntryName = ParameterProcessing.NATIVE + System.getProperty("file.separator") + discoveryFile.getMetadata().getUniqueId() + "_" + discoveryFile.getRealFileName();
        String ExceptionEntryName = ParameterProcessing.EXCEPTION + System.getProperty("file.separator") + discoveryFile.getMetadata().getUniqueId() + "_" + discoveryFile.getRealFileName();
        LOGGER.info("Writing Natie to {}", tmpFolder+nativeEntryName);
        if (documentText != null && documentText.length() > 0) {
            String tepmFolder = project.getResultsDir() + System.getProperty("file.separator") + "tmp" + System.getProperty("file.separator") + textEntryName;
            File f = new File(tepmFolder);
            f.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(tepmFolder));
            writer.write(documentText);
            writer.close();
        }

        columnMetadata.addMetadataValue(DocumentMetadata.TEXT_LINK(), textEntryName);
        if (metadata.get(DocumentMetadataKeys.PROCESSING_EXCEPTION) != null) {
            columnMetadata.addMetadataValue(DocumentMetadata.getLinkException(), ExceptionEntryName);
        } else {
            if (project.isSendIndexToESEnabled()) {
                ESIndex.getInstance().addBatchData(metadata);
            }
            columnMetadata.addMetadataValue(DocumentMetadata.getLinkNative(), nativeEntryName);
        }

        // TODO deal with attachments
        if (metadata.hasParent()) {
            columnMetadata.addMetadataValue(DocumentMetadataKeys.ATTACHMENT_PARENT,
                    ParameterProcessing.DOCTFormat.format(masterOutputFileCount));
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
        appendMetadata(columnMetadata.delimiterSeparatedValues());
    }

    public void packNative() {
        ProcessingStats.getInstance().taskIsNative();
        int indexNativeLink = 0, indexStageFile = 0, indexExceptionLink = 0;
        boolean checkingHeader = true;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
            String line = reader.readLine();
            while (line != null) {
                ArrayList<String> headers = new ArrayList<>();
                Collections.addAll(headers, line.split("\\|"));
                if (checkingHeader) {
                    if (headers.contains("native_link")) {
                        indexNativeLink = headers.indexOf("native_link");
                    }
                    if (headers.contains("exception_link")) {
                        indexExceptionLink = headers.indexOf("exception_link");
                    }
                    if (headers.contains("Source Path")) {
                        indexStageFile = headers.indexOf("Source Path");
                    }
                }
                if (!checkingHeader) {
                    copyNativeFile(indexStageFile, indexNativeLink, headers);
                    copyNativeFile(indexStageFile, indexExceptionLink, headers);
                }
                checkingHeader = false;
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ProcessingStats.getInstance().taskIsCompressing();
        ResultCompressor.getInstance().process();
    }

    private void copyNativeFile(int indexStageFile, int indexExceptionLink, ArrayList<String> headers) throws IOException {
        String newFile;
        File f;
        File stage;
        if (!(newFile = headers.get(indexExceptionLink)).equals("")) {
            f = new File(tmpFolder + System.getProperty("file.separator") + newFile);
            stage = new File(headers.get(indexStageFile));
            f.getParentFile().mkdirs();
            FileUtils.copyFile(stage, f);
            ProcessingStats.getInstance().addNativeCopied(stage.length());
        }
    }

    private void appendMetadata(String string) throws IOException {
        string = string + ParameterProcessing.NL;
        FileUtils.writeStringToFile(metadataFile, string, Charset.defaultCharset(), true);
    }

    /*
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
    */
    public void setup() throws IOException {
        project = Project.getCurrentProject();
        tmpFolder = project.getResultsDir() + System.getProperty("file.separator") + "tmp" + System.getProperty("file.separator");
        columnMetadata = new ColumnMetadata();
        columnMetadata.setFieldSeparator(String.valueOf(Delimiter.getDelim(project.getFieldSeparator())));
        columnMetadata.setAllMetadata(project.getMetadataCollect());
        // write standard metadata fields
        prepareMetadataFile();
        appendMetadata(columnMetadata.delimiterSeparatedHeaders());
    }

    private void prepareMetadataFile() {
        String rootDir = Project.getCurrentProject().getResultsDir();
        String custodian = Project.getCurrentProject().getCurrentCustodian();
//        String custodianExt = custodian.trim().length() > 0 ? "_" + custodian : "";
        //                    + custodianExt + ".csv";
        String metadataFileName = rootDir
                + System.getProperty("file.separator")
                + Project.METADATA_FILE_NAME
//                    + custodianExt + ".csv";
                + ".csv";
        new File(rootDir).mkdir();
        metadataFile = new File(metadataFileName);
        try {
            metadataFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.debug("Filename: {}", metadataFileName);
    }
}
