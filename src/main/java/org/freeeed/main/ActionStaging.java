package org.freeeed.main;

import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.freeeed.services.History;

/**
 *
 * @author mark
 */
public class ActionStaging implements Runnable {

    @Override
    public void run() {
        try {
            stagePackageInput();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void stagePackageInput() throws Exception {
        Configuration processingParameters = FreeEedMain.getInstance().getProcessingParameters();
        History.appendToHistory("Project: " + processingParameters.getString(ParameterProcessing.PROJECT_NAME));
        // TODO better setting of dirs?
        String stagingDir = ParameterProcessing.stagingDir;
        File stagingDirFile = new File(stagingDir);
        if (stagingDirFile.exists()) {
            Files.deleteRecursively(new File(stagingDir));
        }
        new File(stagingDir).mkdirs();

        String[] dirs = processingParameters.getStringArray(ParameterProcessing.PROJECT_INPUTS);
        boolean anyDownload = downloadUri(dirs);
        History.appendToHistory("Packaging and staging the following directories for processing:");
        PackageArchive packageArchive = new PackageArchive();
        // TODO - set custom packaging parameters		
        try {
            for (String dir : dirs) {
                if (new File(dir).exists()) {
                    History.appendToHistory(dir);
                    packageArchive.packageArchive(dir);
                }
            }
            if (anyDownload) {
                History.appendToHistory(ParameterProcessing.DOWNLOAD_DIR);
                packageArchive.packageArchive(ParameterProcessing.DOWNLOAD_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            // follow the "fail-fast" design pattern
            System.exit(0);
        }
        PackageArchive.writeInventory();
        History.appendToHistory("Done");
    }

    private boolean downloadUri(String[] dirs) throws Exception {
        boolean anyDownload = false;
        File downloadDirFile = new File(ParameterProcessing.DOWNLOAD_DIR);
        if (downloadDirFile.exists()) {
            Files.deleteRecursively(downloadDirFile);
        }
        new File(ParameterProcessing.DOWNLOAD_DIR).mkdirs();

        for (String dir : dirs) {
            if (new File(dir).exists()) {
                // skip anything that is a local file
                continue;
            }
            // now assume URI
            URI uri = null;
            String path = null;
            String savePath = null;
            try {
                uri = new URI(dir);
                path = uri.getPath();
                path = StringUtils.replace(path, "/", "");
                savePath = ParameterProcessing.DOWNLOAD_DIR + "/" + path;
            } catch (URISyntaxException e) {
                History.appendToHistory("Incorrect URI syntax, skipping that: " + uri);
                continue;
            }
            try {
                URL url = new URL(dir);
                URLConnection con = url.openConnection();
                BufferedInputStream in =
                        new BufferedInputStream(con.getInputStream());
                FileOutputStream out =
                        new FileOutputStream(savePath);
                History.appendToHistory("Download from " + uri + " to " + savePath);
                int i = 0;
                byte[] bytesIn = new byte[1024];
                while ((i = in.read(bytesIn)) >= 0) {
                    out.write(bytesIn, 0, i);
                }
                out.close();
                in.close();
                anyDownload = true;
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
        return anyDownload;
    }
}
