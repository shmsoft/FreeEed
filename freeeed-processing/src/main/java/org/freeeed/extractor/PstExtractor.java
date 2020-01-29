package org.freeeed.extractor;

import com.shmsoft.jreadpst.*;
import org.freeeed.mr.FreeEedMR;
import org.freeeed.services.Project;

import java.io.File;

public class PstExtractor {

    private Project project;
    private File file;

    public PstExtractor(Project project, File file) {
        this.project = project;
        this.file = file;
    }

    public void extract() {
        Outlook2Eml outlook2Eml = new Outlook2Eml();
        String tmpFolder = project.getStagingDir() + "\\" + file.getName() + "_" + System.currentTimeMillis();
        new File(tmpFolder).mkdirs();
        outlook2Eml.extractEml(file.getAbsolutePath(), tmpFolder, true, true);
        file.delete();
        FreeEedMR.reducePSTFile();
    }
}
