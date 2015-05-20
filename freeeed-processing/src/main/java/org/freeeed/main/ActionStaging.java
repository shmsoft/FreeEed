/*
 *
 * Copyright SHMsoft, Inc. 
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
package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.ui.StagingProgressUI;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class ActionStaging implements Runnable {
    // TODO refactor downloading, eliminate potential UI thread locks
    private static final Logger logger = LoggerFactory.getLogger(ActionStaging.class);
    
    private StagingProgressUI stagingUI;
    private final PackageArchive packageArchive;
    private long totalSize = 0;
    private boolean interrupted = false;
    private String downloadDir;

    public ActionStaging() {
        this.packageArchive = new PackageArchive(null);
    }

    public ActionStaging(StagingProgressUI stagingUI) {
        this.stagingUI = stagingUI;
        this.packageArchive = new PackageArchive(stagingUI);
        this.downloadDir = Settings.getSettings().getDownloadDir();
    }

    @Override
    public void run() {
        try {
            stagePackageInput();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void stagePackageInput() throws Exception {
        Project project = Project.getProject();
        logger.info("Staging project: {}", project.getProjectName());
        String stagingDir = project.getStagingDir();
        File stagingDirFile = new File(stagingDir);

        if (stagingDirFile.exists()) {
            Util.deleteDirectory(stagingDirFile);
        }
        new File(stagingDir).mkdirs();

        setPreparingState();
        calculateSize();

        String[] dirs = project.getInputs();
        String[] custodians = project.getCustodians(dirs);
        // TODO assign custodians to downloads
        boolean anyDownload = downloadUri(dirs);

        setPackagingState();

        logger.info("Packaging and staging the following directories for processing:");

        // TODO - set custom packaging parameters		
        try {
            int urlIndex = -1;
            for (int i = 0; i < dirs.length; ++i) {
                if (interrupted) {
                    break;
                }

                String dir = dirs[i];
                dir = dir.trim();
                if (new File(dir).exists()) {
                    logger.info(dir);
                    project.setCurrentCustodian(custodians[i]);
                    packageArchive.packageArchive(dir);
                } else {
                    urlIndex = i;
                }
            }
            if (!interrupted && anyDownload) {
                logger.info(downloadDir);
                if (urlIndex >= 0) {
                    project.setCurrentCustodian(custodians[urlIndex]);
                }
                packageArchive.packageArchive(downloadDir);
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        PackageArchive.writeInventory();
        logger.info("Done staging");

        setDone();
    }

    private boolean downloadUri(String[] dirs) throws Exception {
        boolean anyDownload = false;
        // TODO until this is fixed, ignore this downloads
        if (true) {
            return false;
        }
        File downloadDirFile = new File(downloadDir);
        if (downloadDirFile.exists()) {
            Util.deleteDirectory(downloadDirFile);
        }
        new File(downloadDir).mkdirs();

        List<DownloadItem> downloadItems = new ArrayList<>();

        for (String dir : dirs) {
            URI uri = null;

            String path;
            String savePath;
            try {
                uri = new URI(dir);
                path = uri.getPath();
                path = StringUtils.replace(path, "/", "");
                savePath = downloadDir + "/" + path;

                DownloadItem di = new DownloadItem();
                di.uri = uri;
                di.file = dir;
                di.savePath = savePath;

                downloadItems.add(di);
            } catch (URISyntaxException e) {
                // TODO maybe not skip but fail?
                logger.error("Incorrect URI syntax, skipping that: " + uri);
                continue;
            }
        }

        setDownloadState(downloadItems.size());

        for (DownloadItem di : downloadItems) {
            try {
                if (interrupted) {
                    return anyDownload;
                }

                setProcessingFile(di.uri.toString());

                URL url = new URL(di.file);
                URLConnection con = url.openConnection();
                try (BufferedInputStream in = new BufferedInputStream(con.getInputStream()); FileOutputStream out = new FileOutputStream(di.savePath)) {
                    logger.info("Download from " + di.uri + " to " + di.savePath);
                    int i;
                    byte[] bytesIn = new byte[1024];
                    while ((i = in.read(bytesIn)) >= 0) {
                        out.write(bytesIn, 0, i);
                    }
                }
                anyDownload = true;

                File downloadedFile = new File(di.savePath);
                totalSize += downloadedFile.length();

                progress(1);
            } catch (Exception e) {
                logger.error("Download error: {}", e.getMessage(), e);
            }
        }
        return anyDownload;
    }

    private void setDownloadState(int size) {
        if (stagingUI != null) {
            stagingUI.setDownloadingState();
            stagingUI.resetCurrentSize();
            stagingUI.setTotalSize(size);
        }
    }

    private void setProcessingFile(String file) {
        if (stagingUI != null) {
            stagingUI.updateProcessingFile(file);
        }
    }

    private void progress(long size) {
        if (stagingUI != null) {
            stagingUI.updateProgress(size);
        }
    }

    private void setDone() {
        if (stagingUI != null) {
            stagingUI.setDone();
        }
    }

    private void setPreparingState() {
        if (stagingUI != null) {
            stagingUI.setPreparingState();
        }
    }

    private void setPackagingState() {
        if (stagingUI != null) {
            stagingUI.setPackagingState();
            stagingUI.resetCurrentSize();
            stagingUI.setTotalSize(totalSize);
        }
    }

    /**
     *
     * @param interrupted
     */
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
        packageArchive.setInterrupted(interrupted);
    }

    private void calculateSize() throws IOException {
        Project project = Project.getProject();
        String[] dirs = project.getInputs();
        totalSize = 0;
        for (String dir : dirs) {
            Path path = Paths.get(dir);
            if (Files.exists(path)) {
                if (Files.isDirectory(path)) {
                    // TODO check for efficiency
                    totalSize += dirSize(path);
                } else {
                    totalSize += Files.size(path);
                }
            }
        }
    }

    private long dirSize(Path path) {
        long size = 0;
        try {
            DirectoryStream ds = Files.newDirectoryStream(path);
            for (Object o: ds) {
                Path p = (Path) o;
                if (Files.isDirectory(p)) {
                    size += dirSize(p);
                } else {
                    size += Files.size(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * Holds download characteristics
     */
    private static final class DownloadItem {

        private String file;
        private URI uri;
        private String savePath;
    }
}
