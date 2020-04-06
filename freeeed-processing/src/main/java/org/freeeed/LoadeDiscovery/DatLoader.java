package org.freeeed.LoadeDiscovery;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.freeeed.services.Project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.freeeed.util.Util;


public class DatLoader {
    private static Project project;
    private static File stagingFolder;
    private static volatile DatLoader mInstance;
    int a = 1;

    private DatLoader() {
    }

    public static DatLoader getInstance() {
        if (mInstance == null) {
            synchronized (DatLoader.class) {
                if (mInstance == null) {
                    mInstance = new DatLoader();
                }
            }
        }
        return mInstance;
    }
    String custodianName ;
    public void run() {
        project = Project.getCurrentProject();
        stagingFolder = new File(project.getStagingDir());





        List<File> files = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY);
        files.forEach(temp -> {
            custodianName = Util.getCustodianFromPath(temp) ;
            String tmpFolder = project.getStagingDir() + System.getProperty("file.separator") + custodianName + System.getProperty("file.separator");
            new File(tmpFolder).mkdirs();
            ZipFile zipFile = new ZipFile(temp);
            try {
                List<FileHeader> fileHeaderList = zipFile.getFileHeaders();
                for (Object o : fileHeaderList) {
                    FileHeader fileHeader = (FileHeader) o;
                    String newFileName;
                    newFileName = fileHeader.getFileName();
                    zipFile.extractFile(fileHeader, tmpFolder, newFileName);
                }
            } catch (net.lingala.zip4j.exception.ZipException e) {
                e.printStackTrace();
            }
            temp.delete();
        });

/*
        List<File> filesDate = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*dat)"), DirectoryFileFilter.DIRECTORY);
        filesDate.forEach(temp -> {
            try {
                List<String> lines = FileUtils.readLines(temp, "ISO-8859-1");
                System.out.println(lines.get(0));
                String string = lines.get(0);
                string = string.replaceAll("\u00FE+$", "").replaceAll("^\u00FE+", "");
                // string = string.replaceAll("^\u00FE+", "");
                String[] parts = string.split("\u00FE\u0014\u00FE");
                int i = 0;
                for (String p : parts) {
                    System.out.println((i++) + ") " + p);
                }
                for (String line : lines) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
*/



        new File(project.getResultsDir()).mkdirs();
        List<File> filesText = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*txt)"), DirectoryFileFilter.DIRECTORY);
        filesText.forEach(temp -> {
            System.out.println(temp.getName());
            Document document = new Document();
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(project.getResultsDir() + System.getProperty("file.separator") + (a++) + ".pdf"));
                document.open();
                File file = new File(temp.getPath());
                String string = FileUtils.readFileToString(file, "UTF-8");
                document.add(new Paragraph(string));
                document.close();
                writer.close();
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
            System.out.println(a);
        });




    }
}
