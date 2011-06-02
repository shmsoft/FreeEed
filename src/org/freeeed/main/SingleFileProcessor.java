package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class SingleFileProcessor extends FileProcessor {
    public SingleFileProcessor(String singleFileName, Context context) {
        super(context);
        setSingleFileName(singleFileName);
    }
    @Override
        public void process() throws IOException {

    }
}
