package org.freeeed.main;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.common.History;

public class PstProcessor {
    private String pstFilePath;
    private Context context;
    
    public PstProcessor(String pstFilePath, Context context) {
        this.pstFilePath = pstFilePath;
        this.context = context;
    }
    // TODO improve PST file type detection
    public static boolean isPST(String fileName) {
        if ("pst".equalsIgnoreCase(Util.getExtension(fileName))) {
            return true;
        }
        return false;
    }
    public void process() throws IOException, Exception {
        String outputDir = "pst_output";
        LinuxUtil.runLinuxCommand("rm -fr " + outputDir);
        extractEmails(pstFilePath, outputDir);
        collectEmails(outputDir);
    }
    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context);
            fileProcessor.process();            
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file: files) {
                collectEmails(file.getPath());
            }
        }                
    }
    /**
     * Extract the emails with appropriate options, follow this sample format
     * readpst -M -D -o myoutput zl_bailey-s_000.pst
     */
    private void extractEmails(String pstPath, String outputDir) throws IOException, Exception {
        String error = LinuxUtil.verifyReadpst();
        if (error != null) {
            History.appendToHistory(error);
            throw new Exception("Not all pre-requisites (readpst for PST processing) are installed");
        }
        new File(outputDir).mkdir();
        String command = "readpst -M -D -o " + outputDir + " " + pstPath;
        LinuxUtil.runLinuxCommand(command);
    }    
}
