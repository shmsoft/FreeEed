package org.freeeed.LoadDiscovery;

import org.apache.commons.io.FileUtils;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CSVProcessor implements LoadDiscoveryFile {
    private final Project project = Project.getCurrentProject();
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataWriter.class);
    private String[] headers;
    String[] fields;

    @Override
    public void processLoadFile() {
        SolrIndex.getInstance().init();

        List<String> inputs = Arrays.asList(project.getInputs());
        inputs.forEach(temp -> {
            try {
                List<String> lines = FileUtils.readLines(new File(temp), "UTF-8");
                fields = getFields(lines.get(0));
                headers = fields;

                for (int i = 1; i < lines.size(); i++) {
                    fields = getFields(lines.get(i));
                    DocumentMetadata metadata = new DocumentMetadata();
                    for (int j = 0; j < headers.length; ++j) {
                        metadata.addField(headers[j], fields[j]);
                    }
                    SolrIndex.getInstance().addBatchData(metadata);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
//        The code below would read the text if it were presentin staging, in the directory
//        But currently we expect the text to be in the "text" field
//        try {
//            File textFile = new File(metadata.getTextLink());
//            if (textFile.exists()) {
//                String text = Files.toString(textFile, Charset.defaultCharset());
//                metadata.setDocumentText(text);
//            }
//        } catch (IOException e) {
//            LOGGER.warn("Cannot read text while importing the load file", e);
//        }
        // SolrIndex.getInstance().destroy();
    }

    private String[] getFields(String line) {
        String sep = ",";
        String sepCode = Project.getCurrentProject().getFieldSeparator();
        if ("pipe".equalsIgnoreCase(sepCode)) {
            sep = "\\|";
        }
        return line.split(sep);
    }
}
