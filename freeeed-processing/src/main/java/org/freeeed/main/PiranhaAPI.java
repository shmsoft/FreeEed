package org.freeeed.main;

import org.freeeed.services.Project;
import org.freeeed.util.OsUtil;

import java.io.IOException;
import java.nio.file.FileSystems;

public class PiranhaAPI {
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
        project.calculateFlatInput();
        String flatInput = project.getFlatInput();
        String command = Project.getCurrentProject().getSparkSubmitCommand() + " "
                + "--master " + Project.getCurrentProject().getSparkMasterURL() + " "
                + "--class x.ProcessFiles " +
                PIRANHA_LIB + " " +
                flatInput + " 2> logs";
        OsUtil.runCommand(command);
    }
}
