package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * How to make zip files as Hadoop input? - was asked on the Hadoop discussion group.
 * The answer was: InputFormat should extend FileInputFormat and 
 * Reader should extend RecordReader.
 */
public class FreeEedProcess extends Configured implements Tool {

	@Override
	public int run(String[] args) throws Exception {
		String testData = "test-output/staging";
		String outputPath = "test-output/output";
		Job job = new Job(getConf());
		job.setJarByClass(FreeEedProcess.class);
		job.setJobName("FreeEedProcessing");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(ArchiveFileInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.setInputPaths(job, new Path(testData));
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
			String line = value.toString();

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
