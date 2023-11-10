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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.main.*;
import org.freeeed.metadata.ColumnMetadata;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Stats;
import org.freeeed.util.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataWriter {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(MetadataWriter.class.getName());
    protected ColumnMetadata columnMetadata;
    private String metadataFileName;
    private File metadataFile;
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();
    protected int masterOutputFileCount;
    protected boolean first = true;
    private LuceneIndex luceneIndex;

    private static String lastParentUPI = null;
//    private boolean firstWriter = true;

    public void processMap(Map<String, String> value) throws IOException, InterruptedException {
        columnMetadata.reinit();

        DocumentMetadata allMetadata = getAllMetadata(value);

        Metadata standardMetadata = getStandardMetadata(allMetadata);
        columnMetadata.addMetadata(standardMetadata);
        columnMetadata.addMetadata(allMetadata);

        if (lastParentUPI == null) lastParentUPI = allMetadata.getUniqueId();
        // Parents and attachments. This solutions assumes all documents are sorted and attachments follow the parent
        if (allMetadata.hasParent()) {
            columnMetadata.addMetadataValue(DocumentMetadataKeys.ATTACHMENT_PARENT, lastParentUPI);
        } else {
            lastParentUPI = allMetadata.getUniqueId();
            columnMetadata.addMetadataValue(DocumentMetadataKeys.ATTACHMENT_PARENT, lastParentUPI);
        }

        String originalFileName = new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
        String documentText = allMetadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        // add the text to the text folder
        String textEntryName = ParameterProcessing.TEXT + "/"
                + allMetadata.getUniqueId() + "_" + originalFileName + ".txt";
        if (textEntryName != null) {
            zipFileWriter.addTextFile(textEntryName, documentText);
        }
        columnMetadata.addMetadataValue(DocumentMetadata.TEXT_LINK(), textEntryName);
        // add the native file to the native folder
        String nativeEntryName = ParameterProcessing.NATIVE + "/"
                + allMetadata.getUniqueId() + "_"
                + originalFileName;
        byte[] bytesWritable = Base64.getDecoder().decode(value.get((ParameterProcessing.NATIVE)));
        if (bytesWritable != null) { // some large exception files are not passed
            zipFileWriter.addBinaryFile(nativeEntryName, bytesWritable, bytesWritable.length);
            LOGGER.fine("Processing file: " + nativeEntryName);
        }
        columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_NATIVE, nativeEntryName);
        // add the pdf made from native to the PDF folder
        String pdfNativeEntryName = ParameterProcessing.PDF_FOLDER + "/"
                + allMetadata.getUniqueId() + "_"
                + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                + ".pdf";
        byte[] pdfBytesWritable = Base64.getDecoder().decode(value.get(ParameterProcessing.NATIVE_AS_PDF));
        if (pdfBytesWritable != null) {
            zipFileWriter.addBinaryFile(pdfNativeEntryName, pdfBytesWritable, pdfBytesWritable.length);
            LOGGER.fine("Processing file: " + pdfNativeEntryName);
        }

        processHtmlContent(value, allMetadata, allMetadata.getUniqueId());

        // add exception to the exception folder
        String exception = allMetadata.get(DocumentMetadataKeys.PROCESSING_EXCEPTION);
        if (exception != null) {
            String exceptionEntryName = "exception/"
                    + allMetadata.getUniqueId() + "_"
                    + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
            if (bytesWritable != null) {
                zipFileWriter.addBinaryFile(exceptionEntryName, bytesWritable, bytesWritable.length);
            }
            columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_EXCEPTION, exceptionEntryName);
        }
        appendMetadata(columnMetadata.delimiterSeparatedValues());
        // prepare for the next file with the same key, if there is any
        first = false;
    }

    private void appendMetadata(String string) throws IOException {
        Files.append(string + ParameterProcessing.NL,
                metadataFile, Charset.defaultCharset());
    }

    private void processHtmlContent(Map<String, String> value, Metadata allMetadata, String uniqueId) throws IOException {
        byte[] htmlBytesWritable =  Base64.getDecoder().decode(value.get(ParameterProcessing.NATIVE_AS_HTML_NAME));
        if (htmlBytesWritable != null) {
            String htmlNativeEntryName = ParameterProcessing.HTML_FOLDER + "/"
                    + uniqueId + "_"
                    + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                    + ".html";
            zipFileWriter.addBinaryFile(htmlNativeEntryName, htmlBytesWritable, htmlBytesWritable.length);
            LOGGER.fine("Processing file: " + htmlNativeEntryName);

            // get the list with other files part of the html output
            String htmlFiles = value.get(ParameterProcessing.NATIVE_AS_HTML);
            if (htmlFiles != null) {
                String fileNames = htmlFiles.toString();
                String[] fileNamesArr = fileNames.split(",");
                for (String fileName : fileNamesArr) {
                    String entry = ParameterProcessing.HTML_FOLDER + "/" + fileName;

                    byte[] imageBytesWritable = Base64.getDecoder().decode(value.get(
                            ParameterProcessing.NATIVE_AS_HTML + "/" + fileName));
                    if (imageBytesWritable != null) {
                        zipFileWriter.addBinaryFile(entry, imageBytesWritable, imageBytesWritable.length);
                        LOGGER.fine("Processing file: " + entry);
                    }
                }
            }
        }
    }

    public void setup() throws IOException {
        Settings settings = Settings.getSettings();
        Project project = Project.getCurrentProject();
        columnMetadata = new ColumnMetadata();
        if (project.getFieldSeparator().equals("DAT")) {
            columnMetadata.setDatOutput(true);
        } else {
            String fileSeparatorStr = project.getFieldSeparator();
            char fieldSeparatorChar = Delimiter.getDelim(fileSeparatorStr);
            columnMetadata.setFieldSeparator(String.valueOf(fieldSeparatorChar));
        }
        columnMetadata.setAllMetadata(project.getMetadataCollect());
        // write standard metadata fields
        prepareMetadataFile();
        if (true
                || Settings.getSettings().isProcessingDistributed()
//                || firstWriter
        ) {
            appendMetadata(columnMetadata.delimiterSeparatedHeaders());
        //    firstWriter = false;
        }
        zipFileWriter.setup();
        zipFileWriter.openZipForWriting();

        luceneIndex = new LuceneIndex(settings.getLuceneIndexDir(), project.getProjectCode(), null);
    }

    private void prepareMetadataFile() {
        String rootDir;
        Project project = Project.getCurrentProject();
        String custodianExt = "";
        if (Project.getCurrentProject().isEnvLocal()) {
            rootDir = Project.getCurrentProject().getResultsDir();
            metadataFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.METADATA_FILE_NAME;
        } else {
            rootDir = ParameterProcessing.TMP_DIR_HADOOP
                    + System.getProperty("file.separator") + "output";
            metadataFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.METADATA_FILE_NAME
                    + custodianExt + "." + project.getMetadataFileExt().toLowerCase();
        }
        new File(rootDir).mkdir();
        String ext = custodianExt + "." + project.getMetadataFileExt().toLowerCase();
        int i = 0;
        while (true) {
            ++i;
            if (new File(metadataFileName + i + ext).exists() == false) {
                metadataFileName += i + ext;
                break;
            }
        }
        metadataFile = new File(metadataFileName);
        LOGGER.fine("Filename: " + metadataFileName);
    }

    public void cleanup()
            throws IOException {
        if (!Project.getCurrentProject().isMetadataCollectStandard()) {
            // write summary headers with all metadata, but for standard metadata don't write the last line
            // context.write(new Text("Hash"), new Text(columnMetadata.delimiterSeparatedHeaders()));
        }
        zipFileWriter.closeZip();

        Stats.getInstance().setJobFinished();
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

    private DocumentMetadata getAllMetadata(Map<String, String> map) {
        DocumentMetadata metadata = new DocumentMetadata();
        Set<String> set = map.keySet();
        Iterator<String> iter = set.iterator();
        while (iter.hasNext()) {
            String name = iter.next().toString();
            if (!ParameterProcessing.NATIVE.equals(name)
                    && !ParameterProcessing.NATIVE_AS_PDF.equals(name)
                    && !name.startsWith(ParameterProcessing.NATIVE_AS_HTML)) { // all metadata but native - which is bytes!
                String value = map.get(name);
                metadata.set(name, value);
            }
        }
        return metadata;
    }
}
