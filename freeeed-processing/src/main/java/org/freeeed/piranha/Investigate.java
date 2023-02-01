package org.freeeed.piranha;

import org.apache.commons.io.FileUtils;
import org.freeeed.main.Version;
import org.freeeed.services.Project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Investigate {
    public static void main(String[] argv) {
        System.out.println("Piranha investigative surgery " + Version.getVersion());
        Investigate instance = new Investigate();
        instance.investigate(argv[0]);
    }

    private void investigate(String dir) {
        System.out.println("Investigating " + dir);
        try {
            addToInventory(dir);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addToInventory(String sourceDirectoryName) throws IOException {
        addDirRecursive(new File(sourceDirectoryName));
    }

    private void addDirRecursive(File fileOrDir) throws IOException {
        if (fileOrDir.isFile()) {
            System.out.println(fileOrDir.getAbsolutePath());
            Project.getCurrentProject().getSummaryMap().addToSummaryMap(fileOrDir);
        } else {
            File[] fileList = fileOrDir.listFiles();
            for (File file : fileList) {
                addDirRecursive(file);
            }
        }
    }
}
