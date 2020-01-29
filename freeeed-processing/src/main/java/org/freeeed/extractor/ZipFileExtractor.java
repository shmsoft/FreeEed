package org.freeeed.extractor;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.Project;

import java.io.File;

public class ZipFileExtractor {

    private Project project;
    private File file;

    public ZipFileExtractor(Project project, File file) {
        this.project = project;
        this.file = file;
    }

    public void extract() {
        String tmpFolder = project.getStagingDir() + "\\" + file.getName() + "_" + System.currentTimeMillis();
        new File(tmpFolder).mkdirs();
        ZipFile zipFile = new ZipFile(file);
        try {
            zipFile.extractAll(tmpFolder);
        } catch (ZipException e) {
            e.printStackTrace();
        }
        file.delete();
        FreeEedMR.reduceZipFile();
    }


}
