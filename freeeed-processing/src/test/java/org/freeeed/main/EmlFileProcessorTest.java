package org.freeeed.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.freeeed.services.Project;
import org.freeeed.util.OsUtil;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class EmlFileProcessorTest {

    @Test
    public void testProcess() throws IOException, InterruptedException {
        // TODO Does not work in Windows, does it even have to?
        if (OsUtil.isWindows()) {
            // no check
        } else {
            Project.getCurrentProject().
                    setEnvironment(Project.ENV_LOCAL).
                    setCurrentCustodian("bob_smith").
                    setTextInMetadata(true);

            Mapper.Context context = mock(Mapper.Context.class);
            doNothing().when(context).progress();
            ArgumentCaptor<Text> arg1 = ArgumentCaptor.forClass(Text.class);
            ArgumentCaptor<MapWritable> arg2 = ArgumentCaptor.forClass(MapWritable.class);
            doNothing().when(context).write(arg1.capture(), arg2.capture());
            EmlFileProcessor emlProcessor = new EmlFileProcessor("test-data/02-loose-files/docs/eml/1.eml", context, null);
            emlProcessor.process(false, null);
            Text hashkey = arg1.getValue();
            assertNotNull(hashkey);
            MapWritable map = arg2.getValue();
            Map<String, String> emlLine = TestUtil.flatten(map);
            assertEquals("bob_smith", emlLine.get("Custodian"));
            assertNotNull(emlLine.get("text"));
            assertTrue(emlLine.get("text").contains("Here are the reports we prepared.  "
                    + "We only trade with 5 of the listed entities.  The reports are done individually by CP b/c "
                    + "we used our canned report (Flowing Deals)  instead of report writer.  "
                    + "If you need any other information or need the information manipulated"));
            assertEquals("\"Denton  Rhonda L.\" <Rhonda.Denton@ENRON.com>", emlLine.get("Message-From"));
            assertEquals("Murphy  Melissa <Melissa.Murphy@ENRON.com> , Bailey  Susan <Susan.Bailey@ENRON.com>", emlLine.get("Message-To"));
            assertEquals("Anderson  Diane <Diane.Anderson@ENRON.com> , Cason  Sharen <Sharen.Cason@ENRON.com>", emlLine.get("Message-Cc"));
            assertEquals("20020201", emlLine.get("Date Received"));
            assertEquals("15:35", emlLine.get("Time Received"));
        }
    }
}
