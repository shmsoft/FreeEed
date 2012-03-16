package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.History;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;

public class Reduce extends Reducer<MD5Hash, MapWritable, Text, Text> {

    protected ColumnMetadata columnMetadata;
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();
    protected int outputFileCount;
    private DecimalFormat UPIFormat = new DecimalFormat("00000");
    private String masterKey;
    private boolean isMaster;

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
        // add the text to the text folder
        String documentText = allMetadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        String textEntryName = ParameterProcessing.TEXT + "/" + UPIFormat.format(outputFileCount) + ".txt";
        if (textEntryName != null) {
            zipFileWriter.addTextFile(textEntryName, documentText);
        }
        // add the native file to the native folder
        String nativeEntryName = ParameterProcessing.NATIVE + "/"
                + UPIFormat.format(outputFileCount) + "_"
                + new File(allMetadata.get(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH)).getName();
        BytesWritable bytesWritable = (BytesWritable) value.get(new Text(ParameterProcessing.NATIVE));
        if (bytesWritable != null) { // some large exception files are not passed
            zipFileWriter.addBinaryFile(nativeEntryName, bytesWritable.getBytes(), bytesWritable.getLength());
            History.appendToHistory(nativeEntryName);
        }
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
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setup(Reducer.Context context)
            throws IOException, InterruptedException {
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
        Project project = Project.getProject();
        if (project.isEnvHadoop()) {
            String outputPath = Project.getProject().getProperty(ParameterProcessing.OUTPUT_DIR_HADOOP);
            String zipFileName = zipFileWriter.getZipFileName();
            String cmd = "";
            if (project.isFsHdfs()) {
                cmd = "hadoop fs -copyFromLocal " + zipFileName + " "
                        + outputPath + File.separator + context.getTaskAttemptID() + ".zip";
            } else if (project.isFsS3()) {
                cmd = "s3cmd put " + zipFileName + " " + Project.getProject().getBucket() // output path has a slash in front
                        + outputPath + File.separator + context.getTaskAttemptID() + ".zip";
            }
            PlatformUtil.runUnixCommand(cmd);
        }
        Stats.getInstance().setJobFinished();
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
            if (!ParameterProcessing.NATIVE.equals(name)) { // all metadata but native - which is bytes!
                Text value = (Text) map.get(new Text(name));
                metadata.set(name, value.toString());
            }
        }
        return metadata;
    }
}
