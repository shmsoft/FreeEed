/*
 *
 * Copyright SHMsoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.services;

import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.mr.ResultCompressor;
import org.freeeed.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mark
 */
public class ProcessingStats {
    private static volatile ProcessingStats mInstance;
    Project project = Project.getCurrentProject();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingStats.class);
    private Date jobStarted = new Date(), jobFinished = new Date();
    private int doneItem = 0, totalItem = 0;
    private StringBuilder messageBuf;
    private FreeEedUIHelper ui = null;
    private long doneSize = 0;
    private int doneNative = 0;
    private int donelException = 0;

    private ProcessingStats() {
        //Singleton
    }

    //Making Stats Thread-Safe
    public static ProcessingStats getInstance() {
        if (mInstance == null) {
            synchronized (ProcessingStats.class) {
                if (mInstance == null) {
                    mInstance = new ProcessingStats();
                }
            }
        }
        return mInstance;
    }

    public void setTotalSize(long totalSize) {
        if (ui != null) {
            ui.setTotalProgressSize(totalSize);
        }
    }

    public void setTotalNative(int totalNative) {
        if (ui != null) {
            ui.setProgressLabel("Copying Native Files...");
            ui.setProgressBarMaximum(totalNative);
            ui.setProgressBarValue(0);
        }
    }

    public void setTotalException(int totalException) {
        if (ui != null) {
            ui.setProgressLabel("Copying Exception Files...");
            ui.setProgressBarMaximum(totalException);
            ui.setProgressBarValue(0);
        }
    }

    public void addDoneException() {
        donelException++;
        if (ui != null) {
            ui.setProgressBarValue(donelException);
        }

    }

    public void addDoneNative() {
        doneNative++;
        if (ui != null) {
            ui.setProgressBarValue(doneNative);
        }
    }

    public void setUi(FreeEedUIHelper ui) {
        this.ui = ui;
        ResultCompressor.getInstance().setUi(ui);
    }

    public void setJobStarted(String projectName) {
        reset();
        jobStarted = new Date();
        messageBuf = new StringBuilder();
        String mes = sdf.format(jobStarted) + "Project " + projectName + " started" + ParameterProcessing.NL;
        messageBuf.append(mes);
        if (ui != null) {
            ui.setProgressLabel("Processing...");
        }
    }

    private void setJobFinished() {
        jobFinished = new Date();
        messageBuf.append(sdf.format(jobFinished)).append("job finished").append(ParameterProcessing.NL);
        messageBuf.append(sdf.format(jobFinished)).append("job duration: ").append(getJobDuration()).append(" sec").append(ParameterProcessing.NL);
        messageBuf.append(sdf.format(jobFinished)).append("item count: ").append(getTotalItem()).append(ParameterProcessing.NL);
        try {
            Util.writeTextFile(project.getResultsDir() + "//report.txt", messageBuf.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        MetadataWriter.getInstance().packNative();
        MetadataWriter.getInstance().packException();
        ResultCompressor.getInstance().process();

        LOGGER.info("ALL DONE");
        reset();
    }

    private void reset() {
        doneItem = totalItem = 0;
    }

    private int getJobDuration() {
        return (int) ((jobFinished.getTime() - jobStarted.getTime()) / 1000);
    }

    public void increaseItemCount(long size) {
        doneItem++;
        doneSize += size;
        if (ui != null) {
            ui.setProgressBarValue(doneItem);
            ui.setProgressedSize(doneSize);
        }

       // System.out.println("=======");
        //System.out.println(doneItem);
        //System.out.println(totalItem);

        if (doneItem == totalItem) {
            setJobFinished();
        }
    }

    /**
     * @return the currentItemCount
     */
    public int getDoneItem() {
        return doneItem;
    }

    /**
     * @return the currentItemTotal
     */
    public int getTotalItem() {
        return totalItem;
    }

    /**
     * @param totalItem the currentItemTotal to set
     */
    public void setTotalItem(int totalItem) {
        this.totalItem = totalItem;
        if (ui != null) {
            ui.setProgressBarMaximum(totalItem);
        }
    }
}
