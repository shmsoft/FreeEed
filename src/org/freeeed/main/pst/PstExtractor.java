package org.freeeed.main.pst;
import org.freeeed.main.Util;

public class PstExtractor {
    private String pstFilePath;
    
    public PstExtractor(String pstFilePath) {
        this.pstFilePath = pstFilePath;
    }
    // TODO improve PST file type detection
    public boolean isPST() {
        if ("pst".equalsIgnoreCase(Util.getExtension(pstFilePath))) {
            return true;
        }
        return false;
    }
    /**
     * Extract the emails with appropriate options, follow this sample format
     * readpst -e -D -o myoutput zl_bailey-s_000.pst
     */
    public void extractEmails(String pstPath, String outputDir) {
         
    }
}
