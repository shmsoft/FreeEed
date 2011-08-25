package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.services.History;

/**
 * Maps input key/value pairs to a set of intermediate key/value pairs.
 *
 * @author mark
 */
public class Map extends Mapper<LongWritable, Text, MD5Hash, MapWritable> {

    /**
     * Called once for each key/value pair in the input split.
     *
     * @param key Key of input
     * @param value Value of input
     * @param context Holds result key/value after process, as well as other params
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        // package (zip) file to be processed
        String zipFile = value.toString();

        // update history
        History.appendToHistory("Processing: " + zipFile);

        // process package (zip) file
        ZipFileProcessor processor = new ZipFileProcessor(zipFile, context);
        processor.process();
    }
}
