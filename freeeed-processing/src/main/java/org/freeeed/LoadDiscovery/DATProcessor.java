package org.freeeed.LoadDiscovery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.main.ZipFileWriter;
import org.freeeed.services.Project;
import org.freeeed.services.UniqueIdGenerator;
import org.freeeed.services.Util;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class DATProcessor implements LoadDiscoveryFile {
    private final Project project = Project.getCurrentProject();
    private static final Logger LOGGER = LoggerFactory.getLogger(DATProcessor.class);
    protected ZipFileWriter zipFileWriter = new ZipFileWriter();

    @Override
    public void processLoadFile() {
        zipFileWriter.setup();
        try {
            zipFileWriter.openZipForWriting();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SolrIndex.getInstance().init();
        List<String> inputs = Arrays.asList(project.getInputs());
        inputs.forEach(temp -> {
            String textFileName = null;
            if (!FilenameUtils.getExtension(temp).equals("dat")) {
                return;
            }
            try {
                List<String> lines = FileUtils.readLines(new File(temp), "UTF-8");
                String titleLine = lines.get(0);
                titleLine = titleLine.replaceAll("\u00FE", "");
                String[] titleParts = titleLine.split("\u0014");
                String dataLine;
                for (int i = 1; i < lines.size(); i++) {
                    dataLine = lines.get(i).replaceAll("\u00FE", "");
                    String[] lineParts = dataLine.split("\u0014");
                    Metadata m = new Metadata();
                    for (int j = 0; j < lineParts.length; j++) {
                        String p = lineParts[j];
                        if (titleParts[j].equals("EXTRACTED TEXT")) {
                            textFileName = p;
                        }
                        m.set(titleParts[j], p);
                    }
                    m.set("UPI", UniqueIdGenerator.getInstance().getNextId());
                    File f;
                    if (textFileName != null) {
                        /**
                         * In linux we have to change \ to / for review to work
                         */
                        if (!OsUtil.isWindows()) {
                            textFileName = textFileName.replace("\\", "/");
                            f = new File(textFileName);
                        } else {
                            f = new File(textFileName);
                        }

                        LOGGER.info("Reading {}", f.getName());
                        List<File> files = (List<File>) FileUtils.listFiles(
                                new File(project.getStagingDir()),
                                new RegexFileFilter(f.getName()),
                                DirectoryFileFilter.DIRECTORY
                        );
                        String text = FileUtils.readFileToString(files.get(0), StandardCharsets.UTF_8);
                        text = text.replaceAll("\r", "<br>").replaceAll("\n", "<br>");
                        text = text.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
                        text = Util.removeNonUtf8CompliantCharacters(text);
                        m.set(DocumentMetadata.DOCUMENT_TEXT, text);
                        m.set("text_link", f.getName());
                        zipFileWriter.addTextFile(f.getName(), text);
                    }
                    SolrIndex.getInstance().addBatchData(m);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        try {
            zipFileWriter.closeZip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SolrIndex.getInstance().flushBatchData();
        SolrIndex.getInstance().destroy();
    }
}
