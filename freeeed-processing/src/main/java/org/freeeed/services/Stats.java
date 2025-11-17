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

import com.google.common.io.Files;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.ui.ProcessProgressUI;
import org.freeeed.util.LogFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mark
 */
public class Stats {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(Stats.class.getName());
    private static final String STATS_FILE_NAME = "logs/stats.txt";
    private static final Stats instance = new Stats();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private Date jobStarted = new Date();
    private Date jobFinished = new Date();
    private int itemCount = 0;
    private int currentItemCountInZip;

    public long getCurrentItemTotalInZip() {
        return currentItemTotalInZip;
    }

    // Number of entries in the current zip file
    private long currentItemTotalInZip;

    public int getCurrentItemZipCount() {
        return currentItemZipCount;
    }
    // Number of zip files processed
    private int currentItemZipCount;

    public int getNumberZipFiles() {
        return numberZipFiles;
    }

    public void setNumberZipFiles(int numberZipFiles) {
        this.numberZipFiles = numberZipFiles;
    }

    private int numberZipFiles = 0;
    private StringBuilder messageBuf;
    private String zipFileName;
    private int numMappers = 0;
    private int mappersProcessed = -1;
    private int piiDocumentsProcessed = 0;
    private int piiCharUnit = 0;
    private int piiDocumentsFound = 0;
    private int summaryDocumentsProcessed = 0;

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
            Util.writeTextFile(STATS_FILE_NAME, messageBuf.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

        reset();
    }

    private void reset() {
        itemCount = 0;
        piiDocumentsProcessed = 0;
        piiCharUnit = 0;
        piiDocumentsFound = 0;
    }

    public int getJobDuration() {
        return (int) ((jobFinished.getTime() - jobStarted.getTime()) / 1000);
    }

    public void increaseItemCount() {
        ++itemCount;
        ++currentItemCountInZip;
        updateProgressReport();
    }

    public int getItemCount() {
        return itemCount;
    }

    public File getStatsFile() {
        return new File(STATS_FILE_NAME);
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
    public void setZipFileForProcessing(String zipFileName) {
        this.zipFileName = zipFileName;
        currentItemCountInZip = 0;
        ++currentItemZipCount;
        currentItemTotalInZip = countNumberItemsInZip(zipFileName);
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ProcessProgressUI.getInstance().reInitComponents();
        }
    }

    private long countNumberItemsInZip(String zipFileName) {
        return new ZipServices().calculateNumberEntriesInZip(zipFileName);
    }
    /**
     * @return the currentItemCount
     */
    //public int getCurrentItemCount() {
    //    return currentItemCount;
    //}

    /**
     * @param currentItemCount the currentItemCount to set
     */
    public void setCurrentItemCount(int currentItemCount) {
        //this.currentItemCount = currentItemCount;
    }

    /**
     * @return the currentItemTotal
     */
//    public int getCurrentItemTotal() {
//        return currentItemTotal;
//    }

    /**
     * @param currentItemTotal the currentItemTotal to set
     */
    public void setCurrentItemTotal(int currentItemTotal) {
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.setTotalSize(currentItemTotal);
        }
    }

    /**
     * @return the numMappers
     */
    public int getNumMappers() {
        return numMappers;
    }

    /**
     * @param numMappers the numMappers to set
     */

    /**
     * @return the mappersProcessed
     */
    public int getMappersProcessed() {
        return mappersProcessed;
    }

    /**
     * @param mappersProcessed the mappersProcessed to set
     */
    public void setMappersProcessed(int mappersProcessed) {
        this.mappersProcessed = mappersProcessed;
    }

    /**
     * Increment count of processed mappers
     */
    public void incrementMapperCount() {
        ++mappersProcessed;
    }

    public synchronized void incrementPiiDocs() {
        ++piiDocumentsProcessed;
    }

    public int getPiiDocumentsProcessed() {
        return piiDocumentsProcessed;
    }

    public synchronized void incrementPiiCharUnit() {
        ++piiCharUnit;
    }

    public int getPiiCharUnit() {
        return piiCharUnit;
    }

    public synchronized void incrementPiiDocsFound() {
        ++piiDocumentsFound;
    }

    public int getPiiDocumentsFound() {
        return piiDocumentsFound;
    }

    public int getSummaryDocumentsProcessed() {
        return summaryDocumentsProcessed;
    }

    public synchronized void incrementSummaryDocumentProcessed() {
        ++summaryDocumentsProcessed;
    }

    public void publishProcessingStatus(String status) {
        String statusFileName = Project.getCurrentProject().getResultsDir() + "/processing_status.txt";
        try {
            Files.write(status, new File(statusFileName), Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.severe("Error writing processing status file: " + statusFileName);
        }
    }
    private void updateProgressReport() {
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        ui.updateProgress(itemCount);
    }

}
