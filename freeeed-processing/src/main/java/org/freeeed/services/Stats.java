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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.freeeed.main.ParameterProcessing;

import org.freeeed.ui.ProcessProgressUI;

/**
 *
 * @author mark 
 */
public class Stats {
    // TODO do stats in a better way
    private static final String statsFileName = "logs/stats.txt";
    private static final Stats instance = new Stats();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private Date jobStarted = new Date();
    private Date jobFinished = new Date();
    private int itemCount = 0;
    private int currentItemCount;
    private int currentItemTotal;
    private StringBuilder messageBuf;
    private String zipFileName;

    private Stats() {
        // singleton
    }

    public static Stats getInstance() {
        return instance;
    }

    /**
     * @return the jobStarted
     */
    public Date getJobStarted() {
        return jobStarted;
    }

    public void setJobStarted(String projectName) {
        jobStarted = new Date();
        messageBuf = new StringBuilder();
        String mes = sdf.format(jobStarted)
                + "Project " + projectName + " started" + ParameterProcessing.NL;
        messageBuf.append(mes);
    }

    public Date getJobFinished() {
        return jobFinished;
    }

    public void setJobFinished() {
        if (!Project.getCurrentProject().isEnvLocal()) {
            return;
        }
        jobFinished = new Date();
        messageBuf.append(sdf.format(jobFinished)).append("job finished").append(ParameterProcessing.NL);
        messageBuf.append(sdf.format(jobFinished)).append("job duration: ").
                append(getJobDuration()).append(" sec").append(ParameterProcessing.NL);
        messageBuf.append(sdf.format(jobFinished)).append("item count: ").
                append(getItemCount()).append(ParameterProcessing.NL);
        try {
            Util.writeTextFile(statsFileName, messageBuf.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        reset();
    }

    private void reset() {
        itemCount = 0;
    }

    public int getJobDuration() {
        return (int) ((jobFinished.getTime() - jobStarted.getTime()) / 1000);
    }

    public void increaseItemCount() {
        ++itemCount;
        ++currentItemCount;
        if (ProcessProgressUI.getInstance() != null) {
            ProcessProgressUI.getInstance().updateProgress(currentItemCount);
        }                   
    }

    public int getItemCount() {
        return itemCount;
    }

    public File getStatsFile() {
        return new File(statsFileName);
    }

    /**
     * @return the zipFileName
     */
    public String getZipFileName() {
        return zipFileName;
    }

    /**
     * @param zipFileName the zipFileName to set
     */
    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
        currentItemCount = 0;        
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.setProcessingState(new File(zipFileName).getName());
            ui.setTotalSize(currentItemTotal);
            ui.updateProgress(0);            
        }
    }

    /**
     * @return the currentItemCount
     */
    public int getCurrentItemCount() {
        return currentItemCount;
    }

    /**
     * @param currentItemCount the currentItemCount to set
     */
    public void setCurrentItemCount(int currentItemCount) {
        this.currentItemCount = currentItemCount;     
    }

    /**
     * @return the currentItemTotal
     */
    public int getCurrentItemTotal() {
        return currentItemTotal;
    }

    /**
     * @param currentItemTotal the currentItemTotal to set
     */
    public void setCurrentItemTotal(int currentItemTotal) {
        this.currentItemTotal = currentItemTotal;
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.setTotalSize(currentItemTotal);
        }
    }
}
