package org.freeeed.main;

import org.apache.hadoop.mapreduce.Mapper.Context;

public class ZipFileProcessor extends FileProcessor {
    public ZipFileProcessor(String zipFileName, Context context) {
        super(context);
        setZipFileName(zipFileName);
    }
}
