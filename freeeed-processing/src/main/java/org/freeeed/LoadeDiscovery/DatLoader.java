package org.freeeed.LoadeDiscovery;


import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.ESIndex;
import org.freeeed.services.ProcessingStats;
import org.freeeed.services.Project;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.freeeed.services.UniqueIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatLoader.class);
    private static Project project;
    private static File stagingFolder;
    private static volatile DatLoader mInstance;

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

    public void run() {
        project = Project.getCurrentProject();
        stagingFolder = new File(project.getStagingDir());
        String docid = project.getCustodians(project.getInputs())[0];

        List<File> files = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*zip)"), DirectoryFileFilter.DIRECTORY);
        files.forEach(temp -> {
            String tmpFolder = project.getStagingDir() + System.getProperty("file.separator") + System.getProperty("file.separator");
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


        ProcessingStats.getInstance().taskIsLoading();

        List<File> filesDate = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(.*dat)"), DirectoryFileFilter.DIRECTORY);
        filesDate.forEach(temp -> {
            try {
                List<String> lines = FileUtils.readLines(temp, "ISO-8859-1");
                String titleLine = lines.get(0);
                titleLine = titleLine.replaceAll("\u00FE+$", "").replaceAll("^\u00FE+", "");
                String[] titleParts = titleLine.split("\u00FE\u0014\u00FE");
                String dataLine;
                ProcessingStats.getInstance().setLoadingItemCount(lines.size()-1);
                for (int i = 1; i < lines.size(); i++) {
                    dataLine = lines.get(i).replaceAll("\u00FE+$", "").replaceAll("^\u00FE+", "");
                    String[] lineParts = dataLine.split("\u00FE\u0014\u00FE");
                    Metadata m = new Metadata();
                    for (int j = 0; j < lineParts.length; j++) {
                        String p = lineParts[j];
                        m.set(titleParts[j], p);
                    }
                    String fileName = m.get(docid);
                    List<File> textFile = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(" + fileName + ".*?)"), DirectoryFileFilter.DIRECTORY);
                    if (textFile.size() > 0) {
                        File file = new File(String.valueOf(textFile.get(0)));
                        String string = FileUtils.readFileToString(file, "UTF-8");
                        string = string.replaceAll("\r", "<br>").replaceAll("\n", "<br>");
                        m.set("text", string);
                    }
                    m.set("UPI", UniqueIdGenerator.INSTANCE.getNextDocumentId());
                    LOGGER.info("Logging {}", fileName);
                    ESIndex.getInstance().addBatchData(m, false);
                    ProcessingStats.getInstance().increaseLoadingItemCount(i);
                }
                ProcessingStats.getInstance().jobDone();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


/*
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
*/

    }
}
