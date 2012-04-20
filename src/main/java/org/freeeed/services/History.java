package org.freeeed.services;

import com.google.common.io.Files;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.freeeed.main.FreeEedLogging;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class History {  
      
    private static String historyFileName = FreeEedLogging.history;
    private static History instance = new History();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private long historyLastModified = 0;
    
    private History() {
        // singleton
    }

    public static History getInstance() {
        return instance;
    }
    
    public boolean isHistoryNew() {
        long lastModified = new File(historyFileName).lastModified();
        if (lastModified != historyLastModified) {
            historyLastModified = lastModified;
            return true;
        } else {
            return false;
        }
    }
    public String getHistory() throws Exception {
        String history = "";
        checkHistoryFile();
        history = FreeEedUtil.readTextFile(historyFileName);
        return history;
    }

    /**
     * Create the file if it is not there
     */
    synchronized private void checkHistoryFile() throws Exception {
        if (!new File(historyFileName).exists()) {
            FreeEedUtil.writeTextFile(historyFileName, getFormattedDate() + "History started\n\n");
        }
    }

    private String getFormattedDate() {
        return sdf.format(new Date());
    }

    synchronized public void eraseHistory() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
        Files.copy(new File(historyFileName),
                new File(historyFileName + "." + dateFormat.format(new Date())));        
        new File(historyFileName).delete();
        checkHistoryFile();
    }

    static synchronized public void appendToHistory(String moreHistory) {
        try {
            getInstance().doAppendToHistory(moreHistory);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    synchronized private void doAppendToHistory(String moreHistory) throws Exception {    
        if (Project.getProject().isEnvLocal()) {
            FreeEedUtil.appendToTextFile(historyFileName, getFormattedDate() + moreHistory + ParameterProcessing.NL);
        } else {
            System.out.println(getFormattedDate() + moreHistory);
        }        
    }
}
