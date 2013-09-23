/*    
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Timer;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.ec2.S3Agent;
import org.freeeed.services.History;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Stats;
import org.freeeed.util.ZipUtil;


import com.google.common.io.Files;

public class Reduce extends Reducer<MD5Hash, MapWritable, Text, Text>
        implements ActionListener {

    protected ColumnMetadata columnMetadata;
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();
    protected int outputFileCount;
    private DecimalFormat UPIFormat = new DecimalFormat("00000");
    private String masterKey;
    protected boolean isMaster;
    private Reducer.Context context;
    private LuceneIndex luceneIndex;

    @Override
    public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
            throws IOException, InterruptedException {
        String outputKey = key.toString();
        masterKey = outputKey;
        isMaster = true;
        for (MapWritable value : values) {
            columnMetadata.reinit();
            ++outputFileCount;
            processMap(value);
            // write this all to the reduce map
            context.write(new Text(outputKey), new Text(columnMetadata.delimiterSeparatedValues()));
            isMaster = false;
        }
    }
    
    protected void processMap(MapWritable value) throws IOException {
        Metadata allMetadata = getAllMetadata(value);
        Metadata standardMetadata = getStandardMetadata(allMetadata, outputFileCount);
        columnMetadata.addMetadata(standardMetadata);
        columnMetadata.addMetadata(allMetadata);
        if (!isMaster) {
            columnMetadata.addMetadataValue(DocumentMetadataKeys.MASTER_DUPLICATE,
                    UPIFormat.format(outputFileCount));
        }
        String originalFileName = new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
        // add the text to the text folder
        String documentText = allMetadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        String textEntryName = ParameterProcessing.TEXT + "/" + 
                UPIFormat.format(outputFileCount) + "_" + originalFileName + ".txt";
        if (textEntryName != null) {
            zipFileWriter.addTextFile(textEntryName, documentText);
        }
        columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_TEXT, textEntryName);
        // add the native file to the native folder
        String nativeEntryName = ParameterProcessing.NATIVE + "/"
                + UPIFormat.format(outputFileCount) + "_"
                + originalFileName;
        BytesWritable bytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE));
        if (bytesWritable != null) { // some large exception files are not passed
            zipFileWriter.addBinaryFile(nativeEntryName, bytesWritable.getBytes(), bytesWritable.getLength());
            History.appendToHistory(nativeEntryName);
        }
        columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_NATIVE, nativeEntryName);
        // add the pdf made from native to the PDF folder
        String pdfNativeEntryName = ParameterProcessing.PDF_FOLDER + "/"
                + UPIFormat.format(outputFileCount) + "_"
                + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName()
                + ".pdf";
        BytesWritable pdfBytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE_AS_PDF));
        if (pdfBytesWritable != null) {
            zipFileWriter.addBinaryFile(pdfNativeEntryName, pdfBytesWritable.getBytes(), pdfBytesWritable.getLength());
            History.appendToHistory(pdfNativeEntryName);
        }
        // add exception to the exception folder
        String exception = allMetadata.get(DocumentMetadataKeys.PROCESSING_EXCEPTION);
        if (exception != null) {
            String exceptionEntryName = "exception/"
                    + UPIFormat.format(outputFileCount) + "_"
                    + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
            if (bytesWritable != null) {
                zipFileWriter.addBinaryFile(exceptionEntryName, bytesWritable.getBytes(), bytesWritable.getLength());
            }
            columnMetadata.addMetadataValue(DocumentMetadataKeys.LINK_EXCEPTION, exceptionEntryName);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup(Reducer.Context context)
            throws IOException, InterruptedException {        
        String settingsStr = context.getConfiguration().get(ParameterProcessing.SETTINGS_STR);
        Settings settings = Settings.loadFromString(settingsStr);
        Settings.setSettings(settings);

        String projectStr = context.getConfiguration().get(ParameterProcessing.PROJECT);
        Project project = Project.loadFromString(projectStr);
        if (project.isEnvHadoop()) {
            String metadataFileContents = context.getConfiguration().get(ParameterProcessing.METADATA_FILE);
            new File(ColumnMetadata.metadataNamesFile).getParentFile().mkdirs();
            Files.write(metadataFileContents.getBytes(), new File(ColumnMetadata.metadataNamesFile));
        }
        columnMetadata = new ColumnMetadata();
        String fileSeparatorStr = project.getFieldSeparator();
        char fieldSeparatorChar = Delim.getDelim(fileSeparatorStr);
        columnMetadata.setFieldSeparator(String.valueOf(fieldSeparatorChar));
        columnMetadata.setAllMetadata(project.getMetadataCollect());
        // write standard metadata fields
        context.write(new Text("Hash"), new Text(columnMetadata.delimiterSeparatedHeaders()));
        zipFileWriter.setup();
        zipFileWriter.openZipForWriting();
        
        luceneIndex = new LuceneIndex(ParameterProcessing.LUCENE_INDEX_DIR, project.getProjectCode(), null);
        luceneIndex.init();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void cleanup(Reducer.Context context)
            throws IOException, InterruptedException {
        if (!Project.getProject().isMetadataCollectStandard()) {
            // write summary headers with all metadata, but for standard metadata don't write the last line
            context.write(new Text("Hash"), new Text(columnMetadata.delimiterSeparatedHeaders()));
        }
        zipFileWriter.closeZip();
        
        if(Project.getProject().isLuceneFSIndexEnabled()) {
            mergeLuceneIndex();
        }
        
        Project project = Project.getProject();
        if (project.isEnvHadoop()) {
            String outputPath = Project.getProject().getProperty(ParameterProcessing.OUTPUT_DIR_HADOOP);
            String zipFileName = zipFileWriter.getZipFileName();
            if (project.isFsHdfs()) {
                String cmd = "hadoop fs -copyFromLocal " + zipFileName + " "
                        + outputPath + File.separator + context.getTaskAttemptID() + ".zip";
                PlatformUtil.runUnixCommand(cmd);
            } else if (project.isFsS3()) {
                S3Agent s3agent = new S3Agent();
                String run = project.getRun();
                if (!run.isEmpty()) {
                    run = run + "/";
                }
                String s3key = project.getProjectCode() + File.separator
                        + "output/"
                        + run
                        + "results/"
                        + context.getTaskAttemptID() + ".zip";
                // Keep updating the hadoop progress
                int refreshInterval = 60000;
                Timer timer = new Timer(refreshInterval, this);
                timer.start();
                s3agent.putFileInS3(zipFileName, s3key);
                timer.stop();
            }

        }
        Stats.getInstance().setJobFinished();
    }

    private void mergeLuceneIndex() throws IOException {
        String hdfsLuceneDir = "/" + ParameterProcessing.LUCENE_INDEX_DIR + File.separator 
                                   + Project.getProject().getProjectCode() + File.separator;
        
        String localLuceneTempDir = ParameterProcessing.LUCENE_INDEX_DIR + File.separator 
                                        + "tmp" + File.separator;
        File localLuceneTempDirFile = new File(localLuceneTempDir);
        
        if (localLuceneTempDirFile.exists()) {
            Files.deleteRecursively(localLuceneTempDirFile);
        }
        
        localLuceneTempDirFile.mkdir();
        
        //copy all zip lucene indexes, created by maps to local hd
        String cmd = "hadoop fs -copyToLocal " + hdfsLuceneDir + "* " + localLuceneTempDir;
        PlatformUtil.runUnixCommand(cmd);
        
        //remove the map indexes as they are now copied to local
        String removeOldZips = "hadoop fs -rm " + hdfsLuceneDir + "*";
        PlatformUtil.runUnixCommand(removeOldZips);
        
        History.appendToHistory("Lucene index files collected to: " + localLuceneTempDirFile.getAbsolutePath());
        
        String[] zipFilesArr = localLuceneTempDirFile.list();
        for (String indexZipFileStr : zipFilesArr) {
            String indexZipFileName = localLuceneTempDir + indexZipFileStr;
            String unzipToDir = localLuceneTempDir + indexZipFileStr.replace(".zip", "");
            
            ZipUtil.unzipFile(indexZipFileName, unzipToDir);
            File indexDir = new File(unzipToDir);
            
            FSDirectory fsDir = FSDirectory.open(indexDir);
            luceneIndex.addToIndex(fsDir);
        }
        // TODO check if we need to push the index to S3 or somewhere else
        luceneIndex.destroy();
    }
    
    /**
     * Here we are using the same names as those in
     * standard.metadata.names.properties - a little fragile, but no choice if
     * we want to tie in with the meaningful data
     */
    private Metadata getStandardMetadata(Metadata allMetadata, int outputFileCount) {
        Metadata metadata = new Metadata();
        metadata.set("UPI", UPIFormat.format(outputFileCount));
        String documentOriginalPath = allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH);
        metadata.set("File Name", new File(documentOriginalPath).getName());
        return metadata;
    }

    private Metadata getAllMetadata(MapWritable map) {
        Metadata metadata = new Metadata();
        Set<Writable> set = map.keySet();
        Iterator<Writable> iter = set.iterator();
        while (iter.hasNext()) {
            String name = iter.next().toString();
            if (!ParameterProcessing.NATIVE.equals(name)
            		&& !ParameterProcessing.NATIVE_AS_PDF.equals(name)) { // all metadata but native - which is bytes!
                Text value = (Text) map.get(new Text(name));
                metadata.set(name, value.toString());
            }
        }
        return metadata;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // inform Hadoop that we are alive
        if (context != null) {
            context.progress();
        }
    }
}
