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
}
