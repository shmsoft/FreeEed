package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
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

	private static IntWritable ONE = new IntWritable(1);

	@Override
	public int run(String[] args) throws Exception {
		String inventory = PackageArchive.inventoryFileName;
		String outputPath = args[0];
		Configuration configuration = getConf();
		// I have actually read the code:
		// this is what it is called in Hadoop 0.20
		configuration.setInt("mapred.linerecordreader.maxlength", 50); // read one file path
		// and this is what it is called in Hadoop 0.21
		configuration.setInt("mapreduce.input.linerecordreader.line.maxlength", 50);
		Job job = new Job(configuration);
		job.setJarByClass(FreeEedProcess.class);
		job.setJobName("FreeEedProcess");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(inventory));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		
		boolean success = job.waitForCompletion(true);
		return success ? 0 : 1;
	}

	public static void main(String[] args) throws Exception {
		int ret = ToolRunner.run(new FreeEedProcess(), args);
		System.exit(ret);
	}

	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

		@Override
		public void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			String oneFile = value.toString();
			System.out.println("Ready to process file: " + oneFile);
			ZipFileProcessor processor = new ZipFileProcessor(oneFile);
			processor.process();
		}
	}

	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
}