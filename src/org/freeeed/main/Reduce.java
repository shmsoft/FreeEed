package org.freeeed.main;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Set;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.tika.metadata.Metadata;

public class Reduce extends Reducer<MD5Hash, MapWritable, Text, Text> {

    private ColumnMetadata columnMetadata = new ColumnMetadata();
    private ZipFileWriter zipFileWriter = new ZipFileWriter();
    private int outputFileCount;
    private DecimalFormat outputFileFormat = new DecimalFormat("00000.txt");

    @Override
    public void reduce(MD5Hash key, Iterable<MapWritable> values, Context context)
            throws IOException, InterruptedException {
        String outputKey = key.toString();
        for (MapWritable value : values) {
            ++outputFileCount;
            Metadata metadata = getFromMapWritable(value);
            columnMetadata.addMetadata(metadata);
            columnMetadata.addMetadataValue("file_number", outputFileFormat.format(outputFileCount));
            String documentText = metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
            String entryName = "text/" + outputFileFormat.format(outputFileCount);
            zipFileWriter.addTextFile(entryName, documentText);
            context.write(new Text(outputKey), new Text(columnMetadata.tabSeparatedValues()));
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
    }

    private Metadata getFromMapWritable(MapWritable map) {
        Metadata metadata = new Metadata();
        Set<Writable> set = map.keySet();
        Iterator<Writable> iter = set.iterator();
        while (iter.hasNext()) {
            String name = iter.next().toString();
            Text value = (Text) map.get(new Text(name));
            metadata.set(name, value.toString());
        }
        return metadata;
    }
}
