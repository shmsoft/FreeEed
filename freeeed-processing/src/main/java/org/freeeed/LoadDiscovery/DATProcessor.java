package org.freeeed.LoadDiscovery;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.services.Project;
import org.freeeed.services.UniqueIdGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DATProcessor implements LoadDiscoveryFile {
    private final Project project = Project.getCurrentProject();
    @Override
    public void processLoadFile() {
        SolrIndex.getInstance().init();
        List<String> inputs = Arrays.asList(project.getInputs());
        inputs.forEach(temp -> {
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
                    dataLine = lines.get(i).replaceAll("\u00FE+$", "").replaceAll("^\u00FE+", "");
                    String[] lineParts = dataLine.split("\u00FE\u0014\u00FE");
                    Metadata m = new Metadata();
                    for (int j = 0; j < lineParts.length; j++) {
                        String p = lineParts[j];
                        m.set(titleParts[j], p);
                    }
                    m.set("UPI", UniqueIdGenerator.getInstance().getNextId());
                    //TODO: HIGH PRIORITY -  How to get text files??
                    /*
                    String fileName = m.get(docid);
                    List<File> textFile = (List<File>) FileUtils.listFiles(stagingFolder, new RegexFileFilter("^(" + fileName + ".*?)"), DirectoryFileFilter.DIRECTORY);
                    if (textFile.size() > 0) {
                        File file = new File(String.valueOf(textFile.get(0)));
                        String string = FileUtils.readFileToString(file, "UTF-8");
                        string = string.replaceAll("\r", "<br>").replaceAll("\n", "<br>");
                        m.set("text", string);
                    }
                    LOGGER.info("Logging {}", fileName);
                    ESIndex.getInstance().addBatchData(m, false);
                   */
                    SolrIndex.getInstance().addBatchData(m);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        SolrIndex.getInstance().flushBatchData();
        SolrIndex.getInstance().destroy();
    }
}
