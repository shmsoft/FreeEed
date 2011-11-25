package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.freeeed.services.History;

/**
 * Maps input key/value pairs to a set of intermediate key/value pairs.
 *
 * @author mark
 */
public class Map extends Mapper<LongWritable, Text, MD5Hash, MapWritable> {

    static private OfficeManager officeManager = null;

    public static OfficeManager getOfficeManager() {
        return officeManager;
    }

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

        History.appendToHistory("Processing: " + zipFile);

        // process archive file
        ZipFileProcessor processor = new ZipFileProcessor(zipFile, context);
        processor.process();
    }

    @Override
    protected void setup(Mapper.Context context) {
        String status = PlatformUtil.verifyWkhtmltopdf();
        if (status != null) {
            System.out.println("Warning: " + status);
        }
        if (FreeEedMain.getInstance().getProcessingParameters().containsKey(ParameterProcessing.CREATE_PDF)) {
            officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
            officeManager.start();
        }
    }

    @Override
    protected void cleanup(Mapper.Context context) {
        if (FreeEedMain.getInstance().getProcessingParameters().containsKey(ParameterProcessing.CREATE_PDF)) {
            officeManager.stop();
        }
    }
}
