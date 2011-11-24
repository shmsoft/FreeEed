package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * Process email files
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
        // TODO - email title?
        processFileEntry(getSingleFileName(), "email.eml");
    }

    @Override
    String getOriginalDocumentPath(String tempFile, String originalFileName) {
        String pathToEmail = tempFile.substring(ParameterProcessing.PST_OUTPUT_DIR.length() + 1);
        return new File(pathToEmail).getParent() + File.separator + originalFileName;
    }
}
