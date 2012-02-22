package org.freeeed.services;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.freeeed.main.FreeEedLogging;

/**
 *
 * @author mark Only for local mode
 */
public class Stats {

    private static String statsFileName = FreeEedLogging.stats;
    private static Stats instance = new Stats();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private Date jobStarted = new Date();
    private Date jobFinished = new Date();
    private int itemCount = 0;
    private StringBuilder messageBuf;

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
                + "Project " + projectName + " started" + Util.NL;
        messageBuf.append(mes);
    }

    public Date getJobFinished() {
        return jobFinished;
    }

    public void setJobFinished() {        
        if (Util.getEnv() != Util.ENV.LOCAL) {
            return;
        }
        jobFinished = new Date();
        messageBuf.append(sdf.format(jobFinished) + "job finished" + Util.NL);
        messageBuf.append(sdf.format(jobFinished) + "job duration: "
                + getJobDuration() + " sec" + Util.NL);
        messageBuf.append(sdf.format(jobFinished) + "item count: "
                + getItemCount() + Util.NL);
        if (Util.getEnv() == Util.ENV.LOCAL) {
            try {
                Util.writeTextFile(statsFileName, messageBuf.toString());
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }                                
        } else {
            System.out.println(messageBuf.toString());
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
