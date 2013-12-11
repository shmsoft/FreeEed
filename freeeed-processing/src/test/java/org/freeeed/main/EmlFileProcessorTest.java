package org.freeeed.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.freeeed.services.Project;
import org.freeeed.util.CsvMetadataParser;
import org.junit.Test;

import com.google.common.io.Files;
import java.io.IOException;
import org.apache.hadoop.mapreduce.Mapper;
import static org.mockito.Mockito.*; 

public class EmlFileProcessorTest {

    @Test
    public void testProcess() throws IOException, InterruptedException {
        Project project = Project.getProject();
        project.setEnvironment(Project.ENV_LOCAL);
        String runDir = "123";
        project.setProperty(ParameterProcessing.RUN, runDir);
        project.setProperty(ParameterProcessing.PROJECT_CODE, "test");
        project.setCurrentCustodian("bob_smith");
        project.setTextInMetadata(true);

        Mapper.Context context = mock(Mapper.Context.class);
        doNothing().when(context).progress();        
        doNothing().when(context).write(this, this);
        EmlFileProcessor emlProcessor = new EmlFileProcessor("test-data/02-loose-files/docs/eml/1.eml", context, null);
        try {
            File outputDir = Files.createTempDir();
            //FileUtils.deleteDirectory(new File("freeeed-output/test/output/" + runDir));
            // Stats.getInstance().setJobStarted(Project.getProject().getProjectName());
            emlProcessor.process();

            //WindowsReduce.getInstance().cleanup(null);

            List<String> lines = Files.readLines(new File(outputDir.getPath() + runDir + "/results/metadata.txt"), 
                    Charset.forName("UTF-8"));
            assertNotNull(lines);
            assertTrue(lines.size() == 4);

            CsvMetadataParser parser = new CsvMetadataParser("\t");
            Map<String, Map<String, String>> data = parser.parseLines(lines);

            Map<String, String> emlLine = data.get("1.eml");
            assertNotNull(emlLine);

            assertEquals("00001", emlLine.get("UPI"));
            assertEquals("bob_smith", emlLine.get("Custodian"));
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

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }
}
