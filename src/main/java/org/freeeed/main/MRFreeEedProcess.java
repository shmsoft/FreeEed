package org.freeeed.main;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.freeeed.main.PlatformUtil.PLATFORM;

/**
 * Configure and start Hadoop process
 *
 * A note on the design: we are reading the file inventory. 
 * We are also setting the max line to read to 10, so that 
 * only one line is read at a time and is given to the Mapper.
 * We could have read the file list in the directory - 
 * but it would have been more work with FileFormat and RecordReader.
 *
 * @param args[0] output directory to hold search results
 */
public class MRFreeEedProcess extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        // inventory dir holds all package (zip) files resulting from stage
        String inventory = args[0];
        String outputPath = args[1];
        System.out.println("Running Hadoop job");
        System.out.println("Inventory = " + inventory);
        System.out.println("Output path = " + outputPath);
        
        // Hadoop configuration class
        Configuration configuration = getConf();

        // I have actually read the Hadoop code
        // this is what it is called in Hadoop 0.20
        configuration.setInt("mapred.linerecordreader.maxlength", 50); // limit so as to read one file path per node
        // and this is what it is called in Hadoop 0.21
        configuration.setInt("mapreduce.input.linerecordreader.line.maxlength", 50);

        Job job = new Job(configuration);
        job.setJarByClass(MRFreeEedProcess.class);
        job.setJobName("FreeEedProcess");

        // Hadoop processes key-value pairs
        job.setOutputKeyClass(MD5Hash.class);
        job.setOutputValueClass(MapWritable.class);

        // set map and reduce classes
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        // Hadoop TextInputFormat class
        // plain text files with linefeed or carriage-return used to signed eol
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // configure Hadoop input files
        // TODO in the future use NLineInputFormat
        // TODO for now, convert inventory into input paths separated by commas
        FileInputFormat.setInputPaths(job, new Path(inventory));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        configuration.set("mapred.reduce.tasks.speculative.execution", "false");

        // current decision to have one reducer -
        // combine all metadata in one place
        job.setNumReduceTasks(1);

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        PLATFORM platform = PlatformUtil.getPlatform();
        int ret = 0;
        switch (platform) {
            case MACOSX:
            case LINUX:
                ret = ToolRunner.run(new MRFreeEedProcess(), args);
                break;
            case WINDOWS:
                WindowsRunner.run(args);
                break;
            default:
                System.out.println("Unknown platform: " + platform);
        }
    }
}