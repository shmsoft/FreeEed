package org.freeeed.main;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.hadoop.io.MD5Hash;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class PiranhaProcessor extends FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PiranhaProcessor.class);
    private static final String PIRANHA_LIB = "lib/piranha_2.12-1.0.jar";

    /**
     * Constructor
     *
     * @param metadataWriter
     * @param luceneIndex
     */
    public PiranhaProcessor(MetadataWriter metadataWriter, LuceneIndex luceneIndex) {
        super(metadataWriter, luceneIndex);
    }

    public String getMetadata(String filePath) throws IOException, InterruptedException {
        String metadata = "Metadata from Piranha: ";
        if (PstProcessor.isPST(filePath)) {
//            new PstProcessor(filePath, metadataWriter, getLuceneIndex()).process();
        } else {
//            String originalFileName = filePath;
//            boolean isAttachment = false;
//            MD5Hash hash = MD5Hash.digest(originalFileName.getBytes(Charsets.UTF_8));
//            DiscoveryFile discoveryFile = new DiscoveryFile(filePath, originalFileName, isAttachment, hash);
//            discoveryFile.setCustodian("Need custodian!");
//            processFileEntry(discoveryFile);
        }
        return metadata;
    }
    public static void startPiranha() throws IOException {
        Project project = Project.getCurrentProject();
        String saveFileName = project.getProjectFileLocation();
        project.getFlatInput(); // this sets the flat file. I know it's bad but I documented it
        try {
            Files.write(project.toString(), new File(saveFileName), Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Cannot save to file " + saveFileName);
        }
        String command = project.getSparkSubmitCommand() + " "
                + "--master " + Project.getCurrentProject().getSparkMasterURL() + " "
                + "--class piranha.ProcessFiles " +
                PIRANHA_LIB + " " +
                project.getProjectFileLocation() + " 2> logs/piranha.log";
        OsUtil.runCommand(command);
    }
    /**
     * Process file
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process(boolean hasAttachments, MD5Hash hash) throws IOException, InterruptedException {
        String emailPath = getSingleFileName();
        String emailName = new File(emailPath).getName();
        // TODO this is a little more complex, there are attachments without extensions
        // if the file already has an extension - then it is an attachment
        String ext = Util.getExtension(emailName);
        if (ext.isEmpty()) {
            emailName += ".eml";
        }

        LOGGER.debug("Processing eml file with path: " + emailPath + ", name: " + emailName);
        processFileEntry(new DiscoveryFile(emailPath, emailName, hasAttachments, hash));
    }
    @Override
    String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        String pathToEmail = discoveryFile.getPath().getPath().substring(Settings.getSettings().getPSTDir().length() + 1);
        return new File(pathToEmail).getParent() + File.separator + discoveryFile.getRealFileName();
    }

}
