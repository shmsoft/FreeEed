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

/**
 * A note on the design: we are reading the file inventory. 
 * We are also setting the max line to read to 10, so that 
 * only one line is read at a time and is given to the Mapper.
 * We could have read the file list in the directory - but
 * but it would have been more work with FileFormat and RecordReader.
 */
public class FreeEedProcess extends Configured implements Tool {

    @Override
    public int run(String[] args) throws Exception {
        String inventory = PackageArchive.inventoryFileName;
        String outputPath = args[0];
        Configuration configuration = getConf();
        // I have actually read the Hadoop code
        // this is what it is called in Hadoop 0.20
        configuration.setInt("mapred.linerecordreader.maxlength", 50); // limit so as to read one file path per node
        // and this is what it is called in Hadoop 0.21
        configuration.setInt("mapreduce.input.linerecordreader.line.maxlength", 50);

        Job job = new Job(configuration);
        job.setJarByClass(FreeEedProcess.class);
        job.setJobName("FreeEedProcess");

        job.setOutputKeyClass(MD5Hash.class);
        job.setOutputValueClass(MapWritable.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(inventory));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        // current decision to have one reducer -
        // combine all metadata in one place
        job.setNumReduceTasks(1);

        boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args) throws Exception {
        int ret = ToolRunner.run(new FreeEedProcess(), args);
        //System.exit(ret);
    }
}