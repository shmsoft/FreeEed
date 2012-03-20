package org.freeeed.review;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.freeeed.main.Delim;
import org.freeeed.main.PlatformUtil;
import org.freeeed.services.Project;

/**
 *
 * @author mark
 */
public class HiveAnalyzer implements Runnable {

    private final String TMP = "/tmp/";
    private String resultFileName;
    
    public HiveAnalyzer(String resultFileName) {
        this.resultFileName = resultFileName;
    }
    
    @Override
    public void run() {
        try {
            loadAndDisplay();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void loadAndDisplay() throws IOException {
        // read the first line of the load file
        String headerStr = Files.readFirstLine(new File(resultFileName), Charset.defaultCharset());
        String delimName = Project.getProject().getFieldSeparator();
        char delim = Delim.getDelim(delimName);
        String[] headers = headerStr.split(String.valueOf(delim));
        StringBuilder createScript = new StringBuilder();
        String tableName = "load_file";
        createScript.append("DROP TABLE IF EXISTS " + tableName + ";" + "\n");
        createScript.append("create table " + tableName + " (");
        for (String header : headers) {
            header = header.trim();
            header = header.replaceAll(" ", "_");
            if ("TO".equalsIgnoreCase(header)) header = "Email_" + header;
            if ("FROM".equalsIgnoreCase(header)) header = "Email_" + header;
            if ("CC".equalsIgnoreCase(header)) header = "Email_"  + header;
            if ("BCC".equalsIgnoreCase(header)) header = "Email_"  + header;
            if (!header.isEmpty()) {
                createScript.append(header + " string,");
            } else {
                createScript.append("no_value" + " string,");
            }
        }
        createScript.replace(createScript.length() - 1, createScript.length(), ")");
        createScript.append("\n");
        createScript.append("row format delimited\n");
        createScript.append("fields terminated by '" + Delim.getSpelledDelim(delimName) + "'\n");
        createScript.append("stored as textfile");
        String scriptFile = TMP + "hive_create_table.sql";
        Files.write(createScript.toString(), new File(scriptFile), Charset.defaultCharset());
        String cmd = "hive -f " + scriptFile;
        PlatformUtil.runUnixCommand(cmd);
        
        StringBuilder loadScript = new StringBuilder();
        loadScript.append("load data local inpath '" + resultFileName + "'\n");
        loadScript.append("overwrite into table " + tableName + ";");
        
        String loadFile = TMP + "hive_load_table.sql";
        Files.write(loadScript.toString(), new File(loadFile), Charset.defaultCharset());
        cmd = "hive -f " + loadFile;
        PlatformUtil.runUnixCommand(cmd);        
        cmd = "xterm -e hive";
        PlatformUtil.runUnixCommand(cmd);        
    }
}
