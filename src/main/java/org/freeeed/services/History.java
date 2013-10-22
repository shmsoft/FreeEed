/*    
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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.Exception;

import org.freeeed.main.SHMcloudLogging;

/**
 *
 * @author mark
 */
public class History {

    private static String historyFileName = SHMcloudLogging.history;
    private static History instance = new History();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss   ");
    private long historyLastModified = 0;
    private String lastLine;
    
    private ArrayList<String> historyList = null;
    private int maxRecords = 1000;

    private History() {
        // singleton
        initHistoryList();
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
        StringBuilder history = new StringBuilder();
        //checkHistoryFile();
        //history = FreeEedUtil.readTextFile(historyFileName);
        if (historyList == null){
            initHistoryList();
        }
        for (String entry: historyList.toArray(new String[0])){
            history.append(entry);
        }
        return history.toString();
    }
    
    synchronized private void initHistoryList(){
        if (historyList == null){
            historyList = new ArrayList<String>();
            try {
            File file = new File(historyFileName);
            if (new File(historyFileName).exists()) {
                long skipLength = file.length() - (maxRecords * 1024);
                BufferedReader logFile = new BufferedReader(new FileReader(historyFileName));
                if (skipLength > 0L) {
                    logFile.skip(skipLength);
                }
                String newLine = logFile.readLine();
                if (newLine != null){
                    newLine = logFile.readLine();
                }
                while (newLine != null){
                    appendToHistoryList(newLine + "\n");
                    newLine = logFile.readLine();
                }
            }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
            
            
        }
        
    }
    
    private void appendToHistoryList(String entry){
        if (historyList == null){
            initHistoryList();
        }
        while (historyList.size() >= maxRecords){
            historyList.remove(0);
        }
        historyList.add(entry);
    }

    /**
     * Create the file if it is not there
     */
    synchronized private void checkHistoryFile() throws Exception {
        if (!new File(historyFileName).exists()) {
            FreeEedUtil.writeTextFile(historyFileName, getFormattedDate() + "History started\n\n");
            appendToHistoryList("History started\n\n");
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
        historyList.clear();
        checkHistoryFile();
    }

    static synchronized public void appendToHistory(String moreHistory) {
        try {
            if (moreHistory.trim().isEmpty()) {
                moreHistory = "...\n";
            } else if ((moreHistory + "\n").equals(getInstance().lastLine)) {
                moreHistory = ".";
            } else {
                moreHistory = moreHistory + "\n";
                getInstance().lastLine = moreHistory;
            }            
            getInstance().doAppendToHistory(moreHistory);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    synchronized private void doAppendToHistory(String moreHistory) throws Exception {
        String output = ".".equals(moreHistory) ? "." : getFormattedDate() + moreHistory;
        appendToHistoryList(output);
        if (Project.getProject().isEnvLocal()) {
            FreeEedUtil.appendToTextFile(historyFileName, output);
        } else {
            System.out.print(output);
        }
    }
}
