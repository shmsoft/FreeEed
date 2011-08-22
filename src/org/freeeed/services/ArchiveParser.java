package org.freeeed.services;

import de.schlichtherle.truezip.file.TFile;

/**
 *
 * @author mark
 * 
 * Test code for trying out TrueZip
 * 
 */
public class ArchiveParser {

    public static void main(String argv[]) {
        ArchiveParser instance = new ArchiveParser();
        String archivePath = "/home/mark/projects/FreeEedData/edrm-enron-v2_bailey-s_pst.zip";
        instance.listFiles(new TFile(archivePath));
        archivePath = "/home/mark/projects/FreeEedData/zl_bailey.zip";
        instance.listFiles(new TFile(archivePath));
    }

    public void listFiles(TFile archivePath) {
        System.out.println("Zip entry: " + archivePath);
        TFile[] entries = new TFile(archivePath).listFiles();
        for (TFile tfile : entries) {
            System.out.println("getAbsolutePath: " + tfile.getAbsolutePath());
            System.out.println("getEnclEntryName: " + tfile.getEnclEntryName());
            System.out.println("getInnerEntryName: " + tfile.getInnerEntryName());
            System.out.println("getName: " + tfile.getName());
            if (tfile.isFile()) {
                System.out.println("It's a file!");
            } else {
                listFiles(tfile);
            }
        }
    }
}
