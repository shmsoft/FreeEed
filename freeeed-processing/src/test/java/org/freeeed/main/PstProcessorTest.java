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
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Kerzner
 */
public class PstProcessorTest {

    private Logger logger = LoggerFactory.getLogger(PstProcessor.class);

    /**
     * Test of isPST method, of class PstProcessor.
     */
    @Test
    public void testIsPST() {
        logger.debug("isPST");
        String fileName = "test-data/pst/zl_pereira-s_000.pst";        
        assertTrue(PstProcessor.isPST(fileName));
    }

    /**
     * Test of process method, of class PstProcessor.
     */
    //@Test
    public void testProcess() throws Exception {
        System.out.println("process");
        PstProcessor instance = null;
        instance.process();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of extractEmails method, of class PstProcessor.
     */
    @Test
    public void testExtractEmails() throws Exception {
        System.out.println("extractEmails");
        String pstPath = "test-data/pst/zl_pereira-s_000.pst";
        String outputDir = "tmp/pst-output";
        PstProcessor instance = new PstProcessor(null, null, null);
        instance.extractEmails(pstPath, outputDir);
        // TODO what to test? Number of emails?
    }

    /**
     * Test of actionPerformed method, of class PstProcessor.
     */
    //@Test
    public void testActionPerformed() {
        System.out.println("actionPerformed");
        ActionEvent event = null;
        PstProcessor instance = null;
        instance.actionPerformed(event);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
