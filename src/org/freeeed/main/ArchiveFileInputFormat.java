package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

public class ArchiveFileInputFormat extends FileInputFormat {

	@Override
	public RecordReader createRecordReader(InputSplit is, TaskAttemptContext tac) throws IOException, InterruptedException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected boolean isSplitable(JobContext context,
			Path filename) {
		// all our inputs are archive files, they are never splittable
		// but should be processed as a whole
		return false;
	}
}
