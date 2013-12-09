package org.freeeed.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.freeeed.util.CsvMetadataParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import java.io.IOException;

public class EmlFileProcessorTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testProcess() throws IOException, InterruptedException {
        Project project = Project.getProject();
        project.setEnvironment(Project.ENV_LOCAL);
        project.setProperty(ParameterProcessing.RUN, "234");
        project.setProperty(ParameterProcessing.PROJECT_CODE, "test");
        Project.getProject().setCurrentCustodian("ivan");
        Project.getProject().setTextInMetadata(true);

        System.setProperty("os.name", "windows");
        EmlFileProcessor emlProcessor = new EmlFileProcessor("test-data/02-loose-files/docs/eml/1.eml", null, null);
        FileUtils.deleteDirectory(new File("freeeed-output/test/output/234"));

        emlProcessor.process();

        List<String> lines = Files.readLines(new File("freeeed-output/test/output/234/results/metadata.txt"), Charset.forName("UTF-8"));

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
