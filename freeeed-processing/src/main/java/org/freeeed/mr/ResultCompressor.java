package org.freeeed.mr;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.services.Project;

import java.io.File;

public class ResultCompressor {

    private static ResultCompressor mInstance;
    private FreeEedUIHelper ui = null;


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
        ZipFile zipFile = new ZipFile(project.getResultsDir() + "/native.zip");
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);
        parameters.setIncludeRootFolder(false);
        zipFile.setRunInThread(true);
        try {
            zipFile.addFolder(new File(project.getResultsDir() + "\\tmp\\"), parameters);
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

        System.out.println("COMPRESSING DONE!");
    }
}
