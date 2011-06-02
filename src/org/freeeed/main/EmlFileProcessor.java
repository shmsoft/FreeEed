package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class EmlFileProcessor extends FileProcessor {

    public EmlFileProcessor(String singleFileName, Context context) {
        super(context);
        setSingleFileName(singleFileName);
    }

    @Override
    public void process() throws IOException, InterruptedException {
        processFileEntry(getSingleFileName(), "PST");
    }
}
