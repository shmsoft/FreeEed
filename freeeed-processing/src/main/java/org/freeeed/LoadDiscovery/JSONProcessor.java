package org.freeeed.LoadDiscovery;

import org.apache.commons.io.FileUtils;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.main.DocumentParser;
import org.freeeed.services.Project;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class JSONProcessor implements LoadDiscoveryFile {
    private final Project project = Project.getCurrentProject();

    @Override
    public void processLoadFile() {
        SolrIndex.getInstance().init();
        DocumentMetadata metadata = new DocumentMetadata();
        List<String> inputs = Arrays.asList(project.getInputs());
        inputs.forEach(temp -> {
            try {
                List<String> lines = FileUtils.readLines(new File(temp), "UTF-8");
                lines.forEach(line -> {
                    DocumentParser.getInstance().parseJsonFields(line, metadata);
                    SolrIndex.getInstance().addBatchData(metadata);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        SolrIndex.getInstance().destroy();
    }
}
