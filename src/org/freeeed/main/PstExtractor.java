package org.freeeed.main;
import java.io.File;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class PstExtractor {
    private String pstFilePath;
    private Context context;
    
    public PstExtractor(String pstFilePath, Context context) {
        this.pstFilePath = pstFilePath;
        this.context = context;
    }
    // TODO improve PST file type detection
    public boolean isPST() {
        if ("pst".equalsIgnoreCase(Util.getExtension(pstFilePath))) {
            return true;
        }
        return false;
    }
    private void collectEmails(String emailDir) throws IOException {
        if (new File(emailDir).isFile()) {
            SingleFileProcessor fileProcessor = new SingleFileProcessor(emailDir, context);
            fileProcessor.process();
            return;
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file: files) {
                collectEmails(file.getPath());
            }
        }                
    }
    /**
     * Extract the emails with appropriate options, follow this sample format
     * readpst -e -D -o myoutput zl_bailey-s_000.pst
     */
    private void extractEmails(String pstPath, String outputDir) throws IOException {
        new File(outputDir).mkdir();
        String command = "readpst -e -D -o " + outputDir + " " + pstPath;
        LinuxUtil.runLinuxCommand(command);
        collectEmails(outputDir);
    }
    
}
