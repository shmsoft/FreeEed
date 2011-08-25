package org.freeeed.main;

import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * Process email files
 *
 * @author mark, randall
 */
public class EmlFileProcessor extends FileProcessor {

    /**
     * Constructor
     * 
     * @param singleFileName
     * @param context
     */
    public EmlFileProcessor(String singleFileName, Context context) {
        super(context);
        setSingleFileName(singleFileName);
    }

    /**
     * Process file
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process() throws IOException, InterruptedException {
        // TODO _- email title?
        processFileEntry(getSingleFileName(), "email.eml");
    }
}
