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
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.ec2.S3Agent;
import org.freeeed.main.*;
import org.freeeed.metadata.ColumnMetadata;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Stats;
import org.freeeed.services.Util;
import org.freeeed.util.OsUtil;
import org.freeeed.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataWriter.class);
    protected ColumnMetadata columnMetadata;
    private String metadataFileName;
    private File metadataFile;
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();
    protected int masterOutputFileCount;
    protected boolean first = true;
    private LuceneIndex luceneIndex;

    private static String lastParentUPI = null;

    public void processMap(MapWritable value) throws IOException, InterruptedException {
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
        // add the text to the text folder
        String documentText = allMetadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
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
        BytesWritable bytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE));
        if (bytesWritable != null) { // some large exception files are not passed
            zipFileWriter.addBinaryFile(nativeEntryName, bytesWritable.getBytes(), bytesWritable.getLength());
            LOGGER.trace("Processing file: {}", nativeEntryName);
        }
        columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_NATIVE, nativeEntryName);
        // add the pdf made from native to the PDF folder
        String pdfNativeEntryName = ParameterProcessing.PDF_FOLDER + "/"
                + allMetadata.getUniqueId() + "_"
                + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                + ".pdf";
        BytesWritable pdfBytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE_AS_PDF));
        if (pdfBytesWritable != null) {
            zipFileWriter.addBinaryFile(pdfNativeEntryName, pdfBytesWritable.getBytes(), pdfBytesWritable.getLength());
            LOGGER.trace("Processing file: {}", pdfNativeEntryName);
        }

        processHtmlContent(value, allMetadata, allMetadata.getUniqueId());

        // add exception to the exception folder
        String exception = allMetadata.get(DocumentMetadataKeys.PROCESSING_EXCEPTION);
        if (exception != null) {
            String exceptionEntryName = "exception/"
                    + allMetadata.getUniqueId() + "_"
                    + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
            if (bytesWritable != null) {
                zipFileWriter.addBinaryFile(exceptionEntryName, bytesWritable.getBytes(), bytesWritable.getLength());
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

    private void processHtmlContent(MapWritable value, Metadata allMetadata, String uniqueId) throws IOException {
        BytesWritable htmlBytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE_AS_HTML_NAME));
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
        appendMetadata(columnMetadata.delimiterSeparatedHeaders());
        zipFileWriter.setup();
        zipFileWriter.openZipForWriting();

        luceneIndex = new LuceneIndex(settings.getLuceneIndexDir(), project.getProjectCode(), null);
        luceneIndex.init();
    }

    private void prepareMetadataFile() {
        String rootDir;
        Project project = Project.getCurrentProject();
        String custodianExt = "";
        if (Project.getCurrentProject().isEnvLocal()) {
            rootDir = Project.getCurrentProject().getResultsDir();
            metadataFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.METADATA_FILE_NAME
                    + custodianExt + "." + project.getMetadataFileExt().toLowerCase();
        } else {
            rootDir = ParameterProcessing.TMP_DIR_HADOOP
                    + System.getProperty("file.separator") + "output";
            metadataFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.METADATA_FILE_NAME
                    + custodianExt + "." + project.getMetadataFileExt().toLowerCase();
        }
        new File(rootDir).mkdir();
        metadataFile = new File(metadataFileName);
        LOGGER.debug("Filename: {}", metadataFileName);
    }

    public void cleanup()
            throws IOException {
        if (!Project.getCurrentProject().isMetadataCollectStandard()) {
            // write summary headers with all metadata, but for standard metadata don't write the last line
            // context.write(new Text("Hash"), new Text(columnMetadata.delimiterSeparatedHeaders()));
        }
        zipFileWriter.closeZip();

        if (Project.getCurrentProject().isLuceneIndexEnabled()) {
            mergeLuceneIndex();
        }

        Project project = Project.getCurrentProject();
        if (project.isEnvHadoop()) {
            String outputPath = Project.getCurrentProject().getProperty(ParameterProcessing.OUTPUT_DIR_HADOOP);
            String zipFileName = zipFileWriter.getZipFileName();
            if (project.isFsHdfs()) {
                String cmd = "hadoop fs -copyFromLocal " + zipFileName + " "
                        + outputPath
                        //                        + File.separator + context.getTaskAttemptID() + 
                        + ".zip";
                OsUtil.runCommand(cmd);
            } else if (project.isFsS3()) {
                S3Agent s3agent = new S3Agent();

                String s3key = project.getProjectCode() + File.separator
                        + "output/"
                        + "results/"
                        //                        + context.getTaskAttemptID()
                        + ".zip";
                s3agent.putFileInS3(zipFileName, s3key);
            }

        }
        Stats.getInstance().setJobFinished();
    }

    private void mergeLuceneIndex() throws IOException {
        String luceneDir = Settings.getSettings().getLuceneIndexDir();
        String hdfsLuceneDir = "/" + luceneDir + File.separator
                + Project.getCurrentProject().getProjectCode() + File.separator;

        String localLuceneTempDir = luceneDir + File.separator
                + "tmp" + File.separator;
        File localLuceneTempDirFile = new File(localLuceneTempDir);

        if (localLuceneTempDirFile.exists()) {
            Util.deleteDirectory(localLuceneTempDirFile);
        }

        localLuceneTempDirFile.mkdir();

        //copy all zip lucene indexes, created by maps to local hd
        String cmd = "hadoop fs -copyToLocal " + hdfsLuceneDir + "* " + localLuceneTempDir;
        OsUtil.runCommand(cmd);

        //remove the map indexes as they are now copied to local
        String removeOldZips = "hadoop fs -rm " + hdfsLuceneDir + "*";
        OsUtil.runCommand(removeOldZips);

        LOGGER.trace("Lucene index files collected to: {}", localLuceneTempDirFile.getAbsolutePath());

        String[] zipFilesArr = localLuceneTempDirFile.list();
        for (String indexZipFileStr : zipFilesArr) {
            String indexZipFileName = localLuceneTempDir + indexZipFileStr;
            String unzipToDir = localLuceneTempDir + indexZipFileStr.replace(".zip", "");

            ZipUtil.unzipFile(indexZipFileName, unzipToDir);
            File indexDir = new File(unzipToDir);

            FSDirectory fsDir = FSDirectory.open(indexDir.toPath());
            luceneIndex.addToIndex(fsDir);
        }
        // TODO check if we need to push the index to S3 or somewhere else
        luceneIndex.destroy();
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
