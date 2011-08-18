package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.Stats;

public class Reduce extends Reducer<MD5Hash, MapWritable, Text, Text> {

    private ColumnMetadata columnMetadata = new ColumnMetadata();
    private ZipFileWriter zipFileWriter = new ZipFileWriter();
    private int outputFileCount;
    private DecimalFormat UPIFormat = new DecimalFormat("00000");

    @Override
    public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
            throws IOException, InterruptedException {
        String outputKey = key.toString();
        for (MapWritable value : values) {
            columnMetadata.reinit();
            processMap(value);
            ++outputFileCount;
            // write this all to the reduce map
            context.write(new Text(outputKey), new Text(columnMetadata.tabSeparatedValues()));
        }
    }

    private void processMap(MapWritable value) throws IOException {
        Metadata allMetadata = getAllMetadata(value);
        Metadata standardMetadata = getStandardMetadata(allMetadata, outputFileCount);
        columnMetadata.addMetadata(standardMetadata);
        columnMetadata.addMetadata(allMetadata);
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
        // write standard metadata fields
        context.write(new Text("Hash"), new Text(columnMetadata.tabSeparatedHeaders()));
        zipFileWriter.openZipForWriting();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void cleanup(Reducer.Context context)
            throws IOException, InterruptedException {
        // write summary headers with all metadata
        context.write(new Text("Hash"), new Text(columnMetadata.tabSeparatedHeaders()));
        zipFileWriter.closeZip();
        Stats.getInstance().setJobFinished();
    }

    /**
     * Here we are using the same names as those in standard.metadata.names.properties -
     * a little fragile, but no choice if we want to tie in with the meaningful data
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
