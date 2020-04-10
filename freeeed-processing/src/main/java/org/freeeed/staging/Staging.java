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
package org.freeeed.staging;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.freeeed.blockchain.BlockChainUtil;
import org.freeeed.helpers.FreeEedUIHelper;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mark
 */
public class Staging implements Runnable {

    // TODO refactor downloading, eliminate potential UI thread locks
    private static final Logger LOGGER = LoggerFactory.getLogger(Staging.class);
    /**
     * stagingUI call are GUI thread-safe
     */
    private long totalSize = 0;
    private boolean interrupted = false;
    private String downloadDir;
    private int totalFileCount = 0;
    private FreeEedUIHelper freeEedUIHelper;
    int mode;

    public Staging() {
    }

    public Staging(FreeEedUIHelper freeEedUIHelper, int mode) {
        this.freeEedUIHelper = freeEedUIHelper;
        this.downloadDir = Settings.getSettings().getDownloadDir();
        this.mode = mode;
    }


    public static long size(Path path) {
        final AtomicLong size = new AtomicLong(0);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {

                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    private long getFile(Path dir) throws IOException {
        return Files.walk(dir)
                .parallel()
                .filter(p -> !p.toFile().isDirectory())
                .count();
    }

    int proggressedFile = 0;

    public void stagePackageInput() throws Exception {
        Project project = Project.getCurrentProject();
        LOGGER.info("Staging project: {}/{}", project.getProjectCode(), project.getProjectName());
        String stagingDir = project.getStagingDir();
        totalSize = Util.calculateSize();
        String[] dirs = project.getInputs();
        String[] custodians = project.getCustodians(dirs);
        // TODO assign custodians to downloads
        boolean anyDownload = downloadUri(dirs);
        if (project.getDataSource() == Project.DATA_SOURCE_BLOCKCHAIN) {
            int totalBlocks = project.getBlockTo() - project.getBlockFrom();
            if (totalBlocks > 0) {
                //setSizeForProgressUI(totalBlocks);
                BlockChainUtil.stageBlockRange(project.getBlockFrom(), project.getBlockTo(), this);
            }
            setDone();
            LOGGER.info("Done staging");
        }

        LOGGER.info("Packaging and staging the following directories for processing:");

        for (String dir : dirs) {
            Path source = Paths.get(dir);
            //totalSize += size(source);
            totalFileCount += getFile(source);
        }

        if (freeEedUIHelper != null) {
            freeEedUIHelper.setProgressBarMaximum(totalFileCount);
        }
        if (mode == 1) {
            FileUtils.deleteDirectory(new File(project.getStagingDir()));
        }

        for (int i = 0; i < dirs.length; i++) {
            String dir = dirs[i];
            File source = new File(dir);
            String folderName = new File(dir).getName();
            File dest = null;
            if (project.getDataSource() == Project.DATA_SOURCE_LOAD_FILE) {
                dest = new File(stagingDir + System.getProperty("file.separator") + System.getProperty("file.separator") + folderName);
            }else{
                String custodian = custodians[i];
                custodian = custodian.replace(" ", "_");
                dest = new File(stagingDir + System.getProperty("file.separator") + custodian + System.getProperty("file.separator") + folderName);
            }
            if (source.isDirectory()) {
                dest.mkdirs();
                try {
                    FileUtils.copyDirectory(source, dest, pathname -> setProgressUIMessage(pathname.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                FileUtils.copyFile(source, dest);
                setProgressUIMessage(dest.getName());
            }
        }
        setDone();
        LOGGER.info("Done staging");
    }

    private boolean downloadUri(String[] dirs) throws Exception {
        boolean anyDownload;
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
                LOGGER.error("Incorrect URI syntax, skipping that: " + uri);
            }
        }

        // setSizeForProgressUI(downloadItems.size());

        for (DownloadItem di : downloadItems) {
            try {
                if (interrupted) {
                    return anyDownload;
                }

                setProgressUIMessage(di.uri.toString());

                URL url = new URL(di.file);
                URLConnection con = url.openConnection();
                try (BufferedInputStream in = new BufferedInputStream(con.getInputStream()); FileOutputStream out = new FileOutputStream(di.savePath)) {
                    LOGGER.info("Download from " + di.uri + " to " + di.savePath);
                    int i;
                    byte[] bytesIn = new byte[1024];
                    while ((i = in.read(bytesIn)) >= 0) {
                        out.write(bytesIn, 0, i);
                    }
                }
                anyDownload = true;

                File downloadedFile = new File(di.savePath);
                totalSize += downloadedFile.length();

                // updateUIProgress(1);
            } catch (Exception e) {
                LOGGER.error("Download error: {}", e.getMessage(), e);
            }
        }
        return anyDownload;
    }

    public boolean setProgressUIMessage(String file) {
        proggressedFile++;
        if (freeEedUIHelper != null) {
            freeEedUIHelper.setProgressBarValue(proggressedFile);
            freeEedUIHelper.setProgressLabel(file);
        }
        return true;
    }

    private void setDone() {
        if (freeEedUIHelper != null) {
            freeEedUIHelper.setProgressDone();
        }
    }

    @Override
    public void run() {
        try {
            stagePackageInput();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
