/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.main;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

/**
 *
 * @author Mark Kerzner
 */
public class PstProcessorTest {

    private static Logger logger = LoggerFactory.getLogger(PstProcessor.class);
    private String pstFileName = "test-data/pst/zl_pereira-s_000.pst";

    @BeforeClass
    public static void setUpClass() {
        String status = PlatformUtil.systemCheck();
        logger.info(status);
    }


    /**
     * Test of isPST method, of class PstProcessor.
     */
    @Test
    public void testIsPST() {
        logger.debug("isPST");
        assertTrue(PstProcessor.isPST(pstFileName));
    }

    /**
     * Test of process method, of class PstProcessor.
     */
    @Test
    public void testProcess() throws Exception {
        Mapper.Context context = mock(Mapper.Context.class);
        doNothing().when(context).progress();
        ArgumentCaptor<MD5Hash> arg1 = ArgumentCaptor.forClass(MD5Hash.class);
        ArgumentCaptor<MapWritable> arg2 = ArgumentCaptor.forClass(MapWritable.class);
        doNothing().when(context).write(arg1.capture(), arg2.capture());
        PstProcessor instance = new PstProcessor(pstFileName, context, null);
        instance.process();
        
        List <MD5Hash> hashkeys = arg1.getAllValues();
        assertNotNull(hashkeys);
        assertEquals(hashkeys.size(), 874);
        List <MapWritable> maps = arg2.getAllValues();
        assertNotNull(maps);
        assertEquals(maps.size(), 874);
    }

    /**
     * Test of extractEmails method, of class PstProcessor.
     */
    @Test
    public void testExtractEmails() throws Exception {
        String outputDir = "tmp/pst-output";
        FileUtils.deleteDirectory(new File(outputDir));
        PstProcessor instance = new PstProcessor(pstFileName, null, null);
        instance.extractEmails(outputDir);
        int results = FileUtils.listFiles(new File(outputDir), null, true).size();
        assertTrue("results == 874", results == 874);
    }

    /**
     * Test of actionPerformed method, of class PstProcessor.
     */
    //@Test
    public void testActionPerformed() {
        ActionEvent event = null;
        PstProcessor instance = null;
        instance.actionPerformed(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
