/*    
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

import com.google.common.io.Files;
import java.io.File;
import org.apache.commons.io.FileUtils;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.PlatformUtil;
import org.freeeed.main.PstProcessor;
import org.freeeed.main.PlatformUtil.PLATFORM;
import org.freeeed.services.Project;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Mark
 */
public class PlatformUtilTest {

    private String pstPath = "test-data/03-enron-pst/zl_bailey-s_000.pst";

    public PlatformUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetPlatform() {
        System.out.println("getPlatform");
        PLATFORM platform = PlatformUtil.getPlatform();
        System.out.println("Platform = " + platform);
    }

    @Test
    public void testReadPst() {
        try {
            System.out.println("testReadPst");
            if (new File(ParameterProcessing.PST_OUTPUT_DIR).exists()) {
                Files.deleteRecursively(new File(ParameterProcessing.PST_OUTPUT_DIR));
            }
            // it really does not matter what you set here - as long is this it not null
            Project project = new Project().loadFromFile(new File("sample_freeeed_linux.project"));
            Project.setProject(project);
            PstProcessor pstProcessor = new PstProcessor(pstPath, null, null);
            pstProcessor.extractEmails(pstPath, ParameterProcessing.PST_OUTPUT_DIR);
            int countEmails = FileUtils.listFiles(
                    new File(ParameterProcessing.PST_OUTPUT_DIR),
                    null, true).size();
            System.out.println("countEmails = " + countEmails);
            assert (countEmails == 2178);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assert (false);
        }
    }
}
