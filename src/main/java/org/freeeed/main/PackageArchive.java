package org.freeeed.main;

import java.io.*;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.freeeed.services.History;
import org.freeeed.services.Project;

/**
 * Package the input directories into zip archives. Zip is selected because it
 * allows comments, which contain path, custodian, and later- forensics
 * information.
 */
public class PackageArchive {

    private double gigsPerArchive;
    // these are needed for the internal working of the code, not for outside	
    private int packageFileCount = 0;
    private DecimalFormat packageFileNameFormat = new DecimalFormat("input00000");
    private String packageFileNameSuffix = ".zip";
    static final int BUFFER = 4096;
    static byte data[] = new byte[BUFFER];
    private int filesCount;
    private ZipOutputStream zipOutputStream;
    private FileOutputStream fileOutputStream;
    private String zipFileName;
    private String rootDir;
    private boolean fileSizeReached;

    public PackageArchive() {
        init();
    }

    private void init() {
        gigsPerArchive = Project.getProject().getGigsPerArchive();
    }

    public void packageArchive(String dir) throws Exception {
        rootDir = dir;
        // separate directories will go into separate zip files
        resetZipStreams();
        packageArchiveRecursively(new File(dir));
        if (filesCount > 0) {
            History.appendToHistory("Wrote " + filesCount + " files");
        }
        zipOutputStream.close();
        fileOutputStream.close();
        writeInventory();
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
            double newSizeGigs = (1.
                    * (file.length() + new File(zipFileName).length()))
                    / ParameterProcessing.ONE_GIG;            
            if (newSizeGigs > gigsPerArchive &&
                    filesCount > 0) {
                fileSizeReached = true;
                resetZipStreams();
            }
            ++filesCount;
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
            String relativePath = file.getPath().substring(new File(rootDir).getParent().length() + 1);
            ZipEntry zipEntry = new ZipEntry(relativePath);
            zipOutputStream.putNextEntry(zipEntry);
            // TODO - add zip file comment: custodian, path, other info
            int count;
            while ((count = bufferedInputStream.read(data, 0,
                    BUFFER)) != -1) {
                zipOutputStream.write(data, 0, count);
            }
            bufferedInputStream.close();
            fileInputStream.close();

        } else if (file.isDirectory()) {
            // add all files in a directory
            if (file.canRead() && file.listFiles() != null) {
                for (File f : file.listFiles()) {
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

    private void resetZipStreams() throws Exception {
        ++packageFileCount;
        if (zipOutputStream != null) {
            zipOutputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
        String stagingDir = Project.getProject().getStagingDir();
        new File(stagingDir).mkdirs();
        zipFileName = stagingDir
                + System.getProperty("file.separator")
                + packageFileNameFormat.format(packageFileCount)
                + Project.getProject().getFormattedCustodian()
                + packageFileNameSuffix;
        fileOutputStream = new FileOutputStream(zipFileName);
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        if (filesCount > 0 && fileSizeReached) {
            History.appendToHistory("Wrote " + filesCount + " files");
        }
        History.appendToHistory("Writing output to staging: " + zipFileName);
        filesCount = 0;
        fileSizeReached = false;
    }

    /**
     * Write the list of zip files that has been created - it will be used by
     * Hadoop
     */
    public static void writeInventory() throws IOException {
        Project project = Project.getProject();
        String stagingDir = project.getStagingDir();
        File[] zipFiles = new File(stagingDir).listFiles();
        File inventory = new File(project.getInventoryFileName());
        BufferedWriter out = new BufferedWriter(new FileWriter(inventory, false));
        for (File file : zipFiles) {
            if (file.getName().endsWith(".zip")) {
                out.write(stagingDir + System.getProperty("file.separator")
                        + file.getName() + System.getProperty("line.separator"));
            }
        }
        out.close();
    }
}
