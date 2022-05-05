package org.freeeed.piranha;

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.freeeed.services.Util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PreProcessor {
    private String sourceDirectoryName;
    private String flatInventoryFileName;

    public PreProcessor(String sourceDirectoryName, String flatInventoryFileName) {
        this.sourceDirectoryName = sourceDirectoryName;
        this.flatInventoryFileName = flatInventoryFileName;
    }

    /**
     * Add all files in the sourceDirectoryName to flatInventoryFileName.
     * It can be called multiple times for the same flatInventoryFileName, starting from empty flatInventoryFileName.
     */
    public void addToInventory() throws IOException {
        addDirRecursive(new File(sourceDirectoryName));
    }

    private void addDirRecursive(File fileOrDir) throws IOException {
        if (fileOrDir.isFile()) {
            FileUtils.writeStringToFile(new File(flatInventoryFileName), fileOrDir.getAbsolutePath() + "\n",
                    StandardCharsets.UTF_8, true);
            Project.getCurrentProject().getSummaryMap().addToSummaryMap(fileOrDir);
        } else {
            File[] fileList = fileOrDir.listFiles();
            for (File file : fileList) {
                addDirRecursive(file);
            }
        }
    }
}
