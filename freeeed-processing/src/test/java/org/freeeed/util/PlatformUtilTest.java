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
package org.freeeed.util;

import java.util.List;

import org.freeeed.main.FreeEedMain;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Mark
 */
public class PlatformUtilTest {

    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(FreeEedMain.class.getName());
    @Test
    public void testGetPlatform() {
        assertTrue(OsUtil.isNix() || OsUtil.isWindows());
    }

    @Test
    public void testRunUnixCommand() {
        if (OsUtil.isNix()) {
            try {
                List<String> out = OsUtil.runCommand("ls");
                assertNotNull(out);
            } catch (Exception e) {
                fail("No exceptions expected!");
            }
        }
    }

    //@Test
    public void testGetFileType() {
        assertTrue(OsUtil.getFileType("test-data/02-loose-files/docs/html/01.htm").
                startsWith("HTML document"));
        assertTrue(OsUtil.getFileType("test-data/02-loose-files/docs/pdf/01.pdf").
                startsWith("PDF document"));
        assertTrue(OsUtil.getFileType("test-data/pst/zl_pereira-s_000.pst").
                startsWith("Microsoft Outlook"));

    }


    @Test
    public void testSystemCheck() {
        OsUtil.systemCheck();
        System.out.println("System summary\n" + OsUtil.getSystemSummary());
    }
}
