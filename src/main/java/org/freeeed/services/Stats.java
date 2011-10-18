package org.freeeed.services;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.freeeed.main.FreeEedLogging;
import org.freeeed.main.Util;

/**
 *
 * @author mark
 */
public class Stats {

    private static String statsFileName = FreeEedLogging.stats;
    private static Stats instance = new Stats();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private Date jobStarted = new Date();
    private Date jobFinished = new Date();
    private int itemCount = 0;

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
        try {
            // re-allocate the file
            if (new File(statsFileName).exists()) new File(statsFileName).delete();
            Util.appendToTextFile(statsFileName, sdf.format(jobStarted)
                    + "Project " + projectName + " started" + Util.NL);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public Date getJobFinished() {
        return jobFinished;
    }

    public void setJobFinished() {
        jobFinished = new Date();
        try {
            Util.appendToTextFile(statsFileName,
                    sdf.format(jobFinished) + "job finished" + Util.NL);
            Util.appendToTextFile(statsFileName,
                    sdf.format(jobFinished) + "job duration: "
                    + getJobDuration() + " sec" + Util.NL);
            Util.appendToTextFile(statsFileName,
                    sdf.format(jobFinished) + "item count: "
                    + getItemCount() + Util.NL);

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
    }

    public int getItemCount() {
        return itemCount;
    }
    public File getStatsFile() {
        return new File(statsFileName);
    }
           
}
