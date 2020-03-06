package org.freeeed.extractor;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Project;
import org.freeeed.services.UniqueIdGenerator;
import org.freeeed.util.Util;

import java.io.File;
import java.util.List;

public class ZipFileExtractor implements Runnable {

    private File file;
    private String tmpFolder;

    public ZipFileExtractor(Project project, File file) {
        this.file = file;
        String fileId = UniqueIdGenerator.INSTANCE.getNextZIPFolderId();
        String custodianName = Util.getCustodianFromPath(file);
        tmpFolder = project.getStagingDir() + System.getProperty("file.separator") + custodianName + System.getProperty("file.separator") + fileId + "_" + file.getName() + System.getProperty("file.separator");
        new File(tmpFolder).mkdirs();
    }

    @Override
    public void run() {
        ZipFile zipFile = new ZipFile(file);
        try {
            List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
            for (Object o : fileHeaderList) {
                FileHeader fileHeader = (FileHeader) o;
                String newFileName;
                if (fileHeader.getFileName().endsWith(".zip")) {
                    newFileName = fileHeader.getFileName();
                } else {
                    String FileId = UniqueIdGenerator.INSTANCE.getNextZIPFileId();
                    newFileName = FileId + "_" + fileHeader.getFileName();
                }
                zipFile.extractFile(fileHeader, tmpFolder, newFileName);
                ProcessingStats.getInstance().addzipFilExtractedSize(fileHeader.getUncompressedSize());
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }

        file.delete();
        FreeEedMR.getInstance().reduceZipFile();
    }
}
