package org.freeeed.util;

import javax.swing.SwingWorker;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class FolderOpener extends SwingWorker<Void, Void> {

    private String folderPath;

    public FolderOpener(String folderPath) {
        this.folderPath = folderPath;
    }

    @Override
    protected Void doInBackground() throws Exception {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(folderPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // This method can be used to update the UI after the folder is opened
    @Override
    protected void done() {
        // UI update code here
    }
}

// Usage:
// new FolderOpener("path/to/folder").execute();


