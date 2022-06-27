package org.freeeed.main;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.freeeed.services.Project;
import org.freeeed.ui.ProjectsUI;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class PiranhaAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(PiranhaAPI.class);
    private static final String PIRANHA_LIB = "lib/piranha_2.12-1.0.jar";

    public String getMetadata(String filePath) {
        String metadata = "Metadata from Piranha: ";
        if (PstProcessor.isPST(filePath)) {
//            new PstProcessor(filePath, metadataWriter, getLuceneIndex()).process();
        } else {
//            String originalFileName = filePath;
//            DiscoveryFile discoveryFile = new DiscoveryFile(filePath, originalFileName, isAttachment, hash);
//            discoveryFile.setCustodian("Need custodian!");
//            processFileEntry(discoveryFile);
        }
        return metadata;
    }
    public static void startPiranha() throws IOException {
        Project project = Project.getCurrentProject();
        String saveFileName = project.getProjectFileLocation();
        try {
            Files.write(project.toString(), new File(saveFileName), Charsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Cannot save to file " + saveFileName);
        }
        String command = Project.getCurrentProject().getSparkSubmitCommand() + " "
                + "--master " + Project.getCurrentProject().getSparkMasterURL() + " "
                + "--class piranha.ProcessFiles " +
                PIRANHA_LIB + " " +
                project.getProjectFileLocation() + " 2> logs/piranha.log";
        OsUtil.runCommand(command);
    }
}
