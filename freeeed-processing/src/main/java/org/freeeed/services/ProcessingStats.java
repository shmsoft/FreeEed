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

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.mr.ResultCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mark
 */
public class ProcessingStats {
    private static volatile ProcessingStats mInstance;
    private Project project = Project.getCurrentProject();
    private NumberFormat nf = NumberFormat.getInstance();
    private Logger LOGGER = LoggerFactory.getLogger(ProcessingStats.class);
    private Date jobStarted = new Date(), jobFinished = new Date();
    private FreeEedUIHelper ui = null;

    private int doneItem = 0, totalItem = 0;
    private int zipFilExtracted = 0;
    private int pstFileExtracted = 0;
    private int doneNative = 0;
    private int sizeType = 0;
    private String sizeTypeLabel = "B";
    private String projectName;
    private long doneSize = 0, totalSize = 0;
    private long zipExtractedSize = 0;
    private long pstExtractedSize = 0;
    private long nativeSize = 0;


    private ProcessingStats() {
    }

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

    public void taskIsZip() {
        if (ui != null) {
            ui.setProgressLabel("Extracting Zip files...");
            ui.setProgressIndeterminate(true);
        }
    }

    public void taskIsPST() {
        if (ui != null) {
            ui.setProgressLabel("Extracting PST files...");
            ui.setProgressIndeterminate(true);
        }
    }

    public void taskIsTika() {
        if (ui != null) {
            ui.setProgressLabel("Processing files...");
            ui.setProgressIndeterminate(false);
        }
    }

    public void taskIsNative() {
        if (ui != null) {
            ui.setProgressLabel("Copying Native Files...");
            ui.setProgressIndeterminate(true);
        }
    }

    public void taskIsCompressing() {
        if (ui != null) {
            ui.setProgressIndeterminate(false);
        }
    }

    public void taskIsLoading() {
        if (ui != null) {
            ui.setProgressLabel("Loading files...");
            ui.setProgressIndeterminate(false);
        }
    }

    public synchronized void addzipFilExtracted() {
        zipFilExtracted++;
    }

    public synchronized void addzipFilExtractedSize(long size) {
        zipExtractedSize += size;
        if (ui != null) {
            setIndeterminateProgressSizeLabel(zipExtractedSize);
        }
    }

    public synchronized void addpstFilExtracted() {
        pstFileExtracted++;
    }

    public synchronized void addpstFilExtractedSize(long size) {
        pstExtractedSize += size;
        if (ui != null) {
            setIndeterminateProgressSizeLabel(pstExtractedSize);
        }
    }

    public synchronized void addNativeCopied(long size) {
        nativeSize += size;
        doneNative++;
    }

    public void setTotalSize(long totalSize) {
        if (totalSize > 102400 && totalSize <= 104857600) {
            totalSize = totalSize / 1024;
            sizeTypeLabel = "KB";
            sizeType = 1;
        } else if (totalSize >= 1024 * 1024) {
            totalSize = (totalSize / 1024) / 1024;
            sizeTypeLabel = "MB";
            sizeType = 2;
        }
        this.totalSize = totalSize;
        if (ui != null) {
            ui.setProgressBarMaximum((int) this.totalSize);
        }
    }

    public void setTotalItem(int totalItem) {
        this.totalItem = totalItem;
    }

    public void setUi(FreeEedUIHelper ui) {
        this.ui = ui;
        ResultCompressor.getInstance().setUi(ui);
    }

    public void setJobStarted(String projectName) {
        reset();
        jobStarted = new Date();
        this.projectName = projectName;
    }

    public void setJobFinished() {
        jobFinished = new Date();

        Map<String, Object> result = new HashMap<>();

        result.put("jobName", projectName);
        result.put("duration", getJobDuration());
        result.put("totalItem", totalItem);
        result.put("totalZip", zipFilExtracted);
        result.put("totalPST", pstFileExtracted);
        result.put("totalItemSize", totalSize);
        result.put("totalZipSize", zipExtractedSize);
        result.put("totalPSTSize", pstFileExtracted);
        result.put("totalNativeSize", nativeSize);
        result.put("totalNativeCopied", doneNative);
        result.put("cores", Runtime.getRuntime().availableProcessors());
        result.put("ram", Runtime.getRuntime().totalMemory());

        Gson g = new Gson();

        String ret = g.toJson(result);


        try {
            FileUtils.writeStringToFile(new File(project.getResultsDir() + System.getProperty("file.separator") + "report.json"), ret, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }


        LOGGER.info("ALL DONE");
        reset();
    }

    public void jobDone() {
        ui.setProgressDone();
    }

    public void setLoadingItemCount(int count) {
        ui.setProgressBarMaximum(count);
    }

    public void increaseLoadingItemCount(int c){
        ui.setProgressBarValue(c);
    }

    private void reset() {
        doneItem = totalItem = 0;
    }

    private int getJobDuration() {
        return (int) ((jobFinished.getTime() - jobStarted.getTime()) / 1000);
    }

    public synchronized void increaseItemCount(long size) {
        doneItem++;
        doneSize += size;
        long doneSizeToShow = 0;
        if (doneSize > 102400 && doneSize <= 104857600 && sizeType == 1) {
            doneSizeToShow = doneSize / 1024;
        } else if (doneSize >= 1024 * 1024 && sizeType == 2) {
            doneSizeToShow = (doneSize / 1024) / 1024;
        }
        if (ui != null) {
            ui.setProgressBarValue((int) doneSizeToShow);
            ui.setProgressedSize(nf.format(doneSizeToShow) + sizeTypeLabel + "/" + nf.format(totalSize) + sizeTypeLabel);
        }
        if (doneItem == totalItem) {
            MetadataWriter.getInstance().packNative();
        }
    }

    private void setIndeterminateProgressSizeLabel(long size) {
        String sizeType = "B";
        if (size > 102400 && size <= 104857600) {
            size = size / 1024;
            sizeType = "KB";
        } else if (size >= 1024 * 1024) {
            size = (size / 1024) / 1024;
            sizeType = "MB";
        }
        ui.setProgressedSize(nf.format(size) + " " + sizeType);
    }
}
