package org.freeeed.main;


import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.freeeed.util.CsvMetadataParser;

import com.google.common.io.Files;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
/*
public class EmlFileProcessorTest {
    // TODO Taking this out for now, it tests the same as EmlFileProcessorTest in freeeed-processing
    // we need re-thinking the tests
    // @Test
    public void testProcess() throws IOException, InterruptedException {
        Project project = Project.getCurrentProject();
        project.setEnvironment(Project.ENV_LOCAL);
        project.setProperty(ParameterProcessing.PROJECT_CODE, "test");
        Project.getCurrentProject().setCurrentCustodian("ivan");
        Project.getCurrentProject().setTextInMetadata(true);
        // MK - I don't get setting the OS - it is whatever your OS happens to be, no?
        // System.setProperty("os.name", "windows");
        EmlFileProcessor emlProcessor = new EmlFileProcessor("test-data/02-loose-files/docs/eml/1.eml", null, null);
        FileUtils.deleteDirectory(new File("freeeed-output/test/output/"));

        emlProcessor.process(false, null);

        List<String> lines = Files.readLines(new File("freeeed-output/test/output/results/metadata.csv"), Charset.forName("UTF-8"));

        assertNotNull(lines);
        assertTrue(lines.size() == 2);

        CsvMetadataParser parser = new CsvMetadataParser("\t");
        Map<String, Map<String, String>> data = parser.parseLines(lines);

        Map<String, String> emlLine = data.get("1.eml");
        assertNotNull(emlLine);

        assertEquals("00001", emlLine.get("UPI"));
        assertEquals("ivan", emlLine.get("Custodian"));
        assertNotNull(emlLine.get("text"));
        assertTrue(emlLine.get("text").contains("Here are the reports we prepared.  We only trade with 5 of the listed entities.  The reports are done individually by CP b/c we used our canned report (Flowing Deals)  instead of report writer.  If you need any other information or need the information manipulated"));

        assertEquals("Denton  Rhonda L. <Rhonda.Denton@ENRON.com>", emlLine.get("From"));

        String to = emlLine.get("To");
        assertEquals("Murphy  Melissa <Melissa.Murphy@ENRON.com> , Bailey  Susan <Susan.Bailey@ENRON.com>", to);

        String cc = emlLine.get("CC");
        assertEquals("Anderson  Diane <Diane.Anderson@ENRON.com> , Cason  Sharen <Sharen.Cason@ENRON.com>", cc);

        String dateReceived = emlLine.get("Date Received");
        assertEquals("20020201", dateReceived);

        String timeReceived = emlLine.get("Time Received");
        assertEquals("15:35", timeReceived);
    }
}
*/

