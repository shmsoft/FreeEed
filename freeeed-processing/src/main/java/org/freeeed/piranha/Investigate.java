package org.freeeed.piranha;

import org.freeeed.main.Version;
import org.freeeed.services.Project;
import org.freeeed.services.Util;

import java.io.File;
import java.io.IOException;

/**
 * To deploy
 * Turn on developer services
 * Do 'mvn package"
 * aws s3 cp freeeed-processing/target/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar s3://shmsoft/freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar
 * aws s3api put-object-acl --bucket shmsoft --key freeeed-processing-1.0-SNAPSHOT-jar-with-dependencies.jar --acl public-read
 */
public class Investigate {
    public static void main(String[] argv) {
        System.out.println("Piranha investigative surgery " + Version.getVersion());
        Investigate instance = new Investigate();
        instance.investigate(argv[0]);
        System.out.println("Total interesting files: " + Project.getCurrentProject().getSummaryMap().getTotalFiles());
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
            if (condition(fileOrDir)) {
                System.out.println(fileOrDir.getAbsolutePath());
                Project.getCurrentProject().getSummaryMap().addToSummaryMap(fileOrDir);
            }
        } else {
            File[] fileList = fileOrDir.listFiles();
            for (File file : fileList) {
                addDirRecursive(file);
            }
        }
    }

    private boolean condition(File file) {
        String fileName = file.getName();
        String extension = Util.getExtension(fileName);
        if (("pdf".equalsIgnoreCase(extension) ||
                "xlsx".equalsIgnoreCase(extension) ||
                "xls".equalsIgnoreCase(extension)
        ) &&
                oneOf(fileName)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean oneOf(String fileName) {
        return true;
    }
}
