package org.freeeed.extractor;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.Project;
import org.freeeed.services.UniqueIdGenerator;

import java.io.File;
import java.util.List;

public class ZipFileExtractor implements Runnable {

    private File file;
    String tmpFolder;
    Project project;
    boolean parent = false;
    String FileId = UniqueIdGenerator.INSTANCE.getNextZIPFolderId();

    public ZipFileExtractor(Project project, File file) {
        this.project = project;
        this.file = file;
        tmpFolder = project.getStagingDir() + "\\" + FileId + "_" + file.getName() + "\\";
        new File(tmpFolder).mkdirs();
    }


    @Override
    public void run() {
        ZipFile zipFile = new ZipFile(file);
        try {
            List fileHeaderList = zipFile.getFileHeaders();
            for (int i = 0; i < fileHeaderList.size(); i++) {
                FileHeader fileHeader = (FileHeader) fileHeaderList.get(i);
                String newFileName;
                if (fileHeader.getFileName().endsWith(".zip")) {
                    newFileName = fileHeader.getFileName();
                } else {
                    String FileId = UniqueIdGenerator.INSTANCE.getNextZIPFileId();
                    newFileName = FileId + "_" + fileHeader.getFileName();
                }
                zipFile.extractFile(fileHeader, tmpFolder, newFileName);
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }

        file.delete();
        FreeEedMR.reduceZipFile();
    }
}
