package org.freeeed.mr;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.main.ActionProcessing;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ResultCompressor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultCompressor.class);
    private static ResultCompressor mInstance;
    private FreeEedUIHelper ui = null;
    private static final Logger logger = LoggerFactory.getLogger(ActionProcessing.class);

    private ResultCompressor() {
    }

    public static ResultCompressor getInstance() {
        if (mInstance == null) {
            mInstance = new ResultCompressor();
        }
        return mInstance;
    }

    public void setUi(FreeEedUIHelper ui) {
        this.ui = ui;
    }

    public void process() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Project project = Project.getCurrentProject();
        ZipFile zipFile = new ZipFile(project.getResultsDir() + System.getProperty("file.separator") + "native.zip");
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        parameters.setIncludeRootFolder(false);
        zipFile.setRunInThread(true);
        LOGGER.info("Compressing Project {}", project.getProjectName());
        LOGGER.info("Compressing Folder {}", project.getResultsDir() + System.getProperty("file.separator") + "tmp" + System.getProperty("file.separator"));
        try {
            zipFile.addFolder(new File(project.getResultsDir() + System.getProperty("file.separator") + "tmp" + System.getProperty("file.separator")), parameters);
        } catch (ZipException e) {
            e.printStackTrace();
        }

        if (ui != null) {
            ui.setProgressLabel("Compressing the Results...");
            ui.setProgressBarValue(0);
            ui.setProgressBarMaximum(100);
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            while (progressMonitor.getState() == ProgressMonitor.State.BUSY) {
                System.out.println(progressMonitor.getPercentDone());
                ui.setProgressBarValue(progressMonitor.getPercentDone());
            }
            ui.setProgressDone();
        }
        ProcessingStats.getInstance().setJobFinished();
        LOGGER.info("Compressing Done {}", project.getProjectName());
    }
}
