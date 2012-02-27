package org.freeeed.main;

import org.freeeed.services.FreeEedUtil;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

/**
 * Process attachments to emails, composite files, etc
 */
public class AttachmentFileProcessor extends FileProcessor {

    /**
     * Constructor
     * 
     * @param singleFileName
     * @param context
     */
    public AttachmentFileProcessor(String singleFileName, Context context) {
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
        String emailPath = getSingleFileName();
        String emailName = new File(emailPath).getName();
        // if the file already has an extension - then it is an attachment
        String ext = FreeEedUtil.getExtension(emailName);
        if (ext.isEmpty()) {
            emailName += ".eml";
        } else {
            System.out.println("Warning: Processing " + emailName
                    + ". expected no-extension emails");
        }
        processFileEntry(emailPath, emailName);
    }

    @Override
    String getOriginalDocumentPath(String tempFile, String originalFileName) {
        String pathToEmail = tempFile.substring(ParameterProcessing.PST_OUTPUT_DIR.length() + 1);
        return new File(pathToEmail).getParent() + File.separator + originalFileName;
    }
}
