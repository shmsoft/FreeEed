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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.freeeed.services.Project;
import org.freeeed.ui.StagingProgressUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Package the input directories into zip archives. Zip is selected because it
 * allows comments, which contain path, custodian, and later- forensics
 * information.
 */
public class PackageArchive {

    private static final Logger logger = LoggerFactory.getLogger(PackageArchive.class);
    private double gigsPerArchive;
    // these are needed for the internal working of the code, not for outside	
    private int packageFileCount = 0;
    private final DecimalFormat packageFileNameFormat = new DecimalFormat("input00000");
    private final String packageFileNameSuffix = ".zip";
    static final int BUFFER = 4096;
    static byte data[] = new byte[BUFFER];
    private int filesCount;
    private ZipOutputStream zipOutputStream;
    private FileOutputStream fileOutputStream;
    private String zipFileName;
    private String rootDir;
    private boolean fileSizeReached;
    private final StagingProgressUI stagingUI;
    private boolean interrupted = false;

    public PackageArchive(StagingProgressUI stagingUI) {
        this.stagingUI = stagingUI;
        init();
    }

    private void init() {
        gigsPerArchive = Project.getCurrentProject().getGigsPerArchive();
    }

    public void packageArchive(String dir) throws Exception {
        int dataSource = Project.getCurrentProject().getDataSource();
        if (dataSource == Project.DATA_SOURCE_EDISCOVERY) {
            // if we are packaging a zip file, no need to zip it up. Just copy it.
            // TODO revisit this
            if (new File(dir).isFile() && dir.endsWith(".zip")) {
                Path source = Paths.get(dir);
                Path stagingPath = Paths.get(Project.getCurrentProject().getStagingDir());
                Files.copy(source, stagingPath.resolve(source.getFileName()));
                return;
            }
            rootDir = dir;
            packageArchiveRecursively(new File(dir));
            if (filesCount > 0) {
                logger.info("Wrote {} files", filesCount);
            }
//            zipOutputStream.close();
//            fileOutputStream.close();
        } else if (dataSource == Project.DATA_SOURCE_LOAD_FILE) {
            Path source = Paths.get(dir);
            Path stagingPath = Paths.get(Project.getCurrentProject().getStagingDir());
            Files.copy(source, stagingPath.resolve(source.getFileName()));
        }
    }

    /**
     * TODO: this is taken from an (old) article on compression:
     * http://java.sun.com/developer/technicalArticles/Programming/compression/
     * can it be improved?
     *
     * @param file
     * @param zipOutputStream
     * @throws IOException
     */
    private void packageArchiveRecursively(File file) throws Exception {
        if (file.isFile()) {
            if (stagingUI != null) {
                stagingUI.updateProcessingFile(file.getAbsolutePath());
            }
            // if it is a zip file, 
            double newSizeGigs = (1.
                    * (file.length() + new File(zipFileName).length()))
                    / ParameterProcessing.ONE_GIG;
            if (newSizeGigs > gigsPerArchive
                    && filesCount > 0) {
                fileSizeReached = true;
                resetZipStreams();
            }
            ++filesCount;
            try (FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER)) {

                File rootFile = new File(rootDir);
                String parent = rootFile.getParent();
                String relativePath = file.getPath();
                if (parent != null) {
                    relativePath = file.getPath().substring(new File(rootDir).getParent().length() + 1);
                }

                ZipEntry zipEntry = new ZipEntry(relativePath);
                String description = "Custodian: " + Project.getCurrentProject().getCurrentCustodian() + "\n"
                        + "Source path: " + file.getAbsolutePath();
                zipEntry.setComment(description);
                zipOutputStream.putNextEntry(zipEntry);
                int count;
                while ((count = bufferedInputStream.read(data, 0,
                        BUFFER)) != -1) {
                    zipOutputStream.write(data, 0, count);
                }
            }

            if (stagingUI != null) {
                stagingUI.updateProgress(file.length());
            }

        } else if (file.isDirectory()) {
            // add all files in a directory
            if (file.canRead() && file.listFiles() != null) {
                File[] fileList = file.listFiles();
                Arrays.sort(fileList);
                for (File f : fileList) {
                    if (interrupted) {
                        break;
                    }
                    packageArchiveRecursively(f);
                }
            } else {
                JOptionPane.showMessageDialog(null, "You don't have read access to this file:\n"
                        + file.getPath() + "\n"
                        + "No files will be staged. Please fix the permissions first");
                throw new Exception("No read access to file " + file.getPath());
            }
        }
    }

    public void closeZipStreams() throws Exception {
        if (zipOutputStream != null) {
            zipOutputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
    }

    public void resetZipStreams() throws Exception {
        ++packageFileCount;
        if (zipOutputStream != null) {
            zipOutputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
        String stagingDir = Project.getCurrentProject().getStagingDir();
        new File(stagingDir).mkdirs();
        zipFileName = stagingDir
                + System.getProperty("file.separator")
                + packageFileNameFormat.format(packageFileCount)
                //                + Project.getCurrentProject().getFormattedCustodian()
                + packageFileNameSuffix;
        fileOutputStream = new FileOutputStream(zipFileName);
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        if (filesCount > 0 && fileSizeReached) {
            logger.info("Wrote {} files ", filesCount);
        }
        logger.info("Writing output to staging: {}", zipFileName);
        filesCount = 0;
        fileSizeReached = false;
    }

    /**
     * Write the list of zip files that has been created - it will be used by
     * Hadoop
     *
     * @throws java.io.IOException
     */
    public static void writeInventory() throws IOException {
        Project project = Project.getCurrentProject();
        String stagingDir = project.getStagingDir();
        File[] inventoryFiles = new File(stagingDir).listFiles();
        File inventory = new File(project.getInventoryFileName());
        try (BufferedWriter out = new BufferedWriter(new FileWriter(inventory, false))) {
            for (File file : inventoryFiles) {
                out.write(stagingDir + System.getProperty("file.separator")
                        + file.getName() + System.getProperty("line.separator"));
            }
        }
    }

    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
}
