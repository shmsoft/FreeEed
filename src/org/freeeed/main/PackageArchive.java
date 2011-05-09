package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Package the input directories into zip archives. Zip is selected
 * because it allows comments, which contain path, custodian, and later-
 * forensics information.
 */
public class PackageArchive {

    private int filesPerArchive;
    private ArrayList<String> inputDirs;
    private static final String rootDir = "test-output/staging";
    public static final String inventoryFileName = rootDir + "/inventory";
    // these are needed for the internal working of the code, not for outside	
    private int packageFileCount = 0;
    private DecimalFormat packageFileNameFormat = new DecimalFormat("input00000");
    private String packageFileNameSuffix = ".zip";
    static final int BUFFER = 4096;
    static byte data[] = new byte[BUFFER];
    private int filesCount;
    private ZipOutputStream zipOutputStream;
    private FileOutputStream fileOutputStream;

    public PackageArchive() {
        init();
    }

    private void init() {
        filesPerArchive = FreeEedMain.getInstance().getProcessingParameters().getInt("files.per.zip.staging");
    }

    /**
     * @return the inputDirs
     */
    public ArrayList<String> getInputDirs() {
        return inputDirs;
    }

    /**
     * @param inputDirs the inputDirs to set
     */
    public void setInputDirs(ArrayList<String> inputDirs) {
        this.inputDirs = inputDirs;
    }

    public void packageArchive(String dir) throws IOException {
        // separate directories will go into separate zip files
        resetZipStreams();
        packageArchiveRecursively(new File(dir));
        zipOutputStream.close();
        fileOutputStream.close();
        writeInventory();
    }

    /**
     * TODO: this is taken from an (old) article on compression:
     * http://java.sun.com/developer/technicalArticles/Programming/compression/
     * can it be improved?
     * @param file
     * @param zipOutputStream
     * @throws IOException 
     */
    private void packageArchiveRecursively(File file) throws IOException {
        if (file.isFile()) {
            if (++filesCount > filesPerArchive) {
                resetZipStreams();
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, BUFFER);
            ZipEntry zipEntry = new ZipEntry(file.getPath());
            zipOutputStream.putNextEntry(zipEntry);
            int count;
            while ((count = bufferedInputStream.read(data, 0,
                    BUFFER)) != -1) {
                zipOutputStream.write(data, 0, count);
            }
            bufferedInputStream.close();
            fileInputStream.close();

        } else if (file.isDirectory()) {
            // add all files in a directory
            for (File f : file.listFiles()) {
                packageArchiveRecursively(f);
            }
        }
    }

    private void resetZipStreams() throws IOException {
        ++packageFileCount;
        if (zipOutputStream != null) {
            zipOutputStream.close();
        }
        if (fileOutputStream != null) {
            fileOutputStream.close();
        }
        new File(rootDir).mkdirs();
        String zipFileName = rootDir + System.getProperty("file.separator")
                + packageFileNameFormat.format(packageFileCount)
                + packageFileNameSuffix;
        fileOutputStream = new FileOutputStream(zipFileName);
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        filesCount = 0;
        System.out.println("Writing output to staging: " + zipFileName);
    }

    /**
     * Write the list of zip files that has been created -
     * it will be used by Hadoop
     */
    private void writeInventory() throws IOException {
        File[] zipFiles = new File(rootDir).listFiles();
        File inventory = new File(inventoryFileName);
        BufferedWriter out = new BufferedWriter(new FileWriter(inventory, false));
        for (File file : zipFiles) {
            if (file.getName().endsWith(".zip")) {
                out.write(rootDir + System.getProperty("file.separator")
                        + file.getName() + System.getProperty("line.separator"));
            }
        }
        out.close();
    }
}
