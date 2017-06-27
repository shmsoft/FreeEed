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

import org.freeeed.util.OsUtil;
import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Kerzner
 */
public class PstProcessorTest {

    private static final Logger logger = LoggerFactory.getLogger(PstProcessor.class);
    private final String pstFileName = "test-data/pst/zl_pereira-s_000.pst";

    @BeforeClass
    public static void setUpClass() {
        OsUtil.systemCheck();
        List<String> status = OsUtil.getSystemSummary();
        for (String stat : status) {
            logger.info(stat);
        }
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
     * Test of extractEmails method, of class PstProcessor.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testExtractEmails() throws Exception {
        String outputDir = "tmp/pst-output";
        FileUtils.deleteDirectory(new File(outputDir));
        PstProcessor instance = new PstProcessor(pstFileName, null, null);
        instance.extractEmails(outputDir);
        int results = FileUtils.listFiles(new File(outputDir), null, true).size();
        if (OsUtil.isWindows()) {
            // no checks
        } else if (OsUtil.isMac()) {
            assertEquals(874, results);
        } else if (OsUtil.isLinux()) {
            assertEquals(873, results);
        }
    }
}