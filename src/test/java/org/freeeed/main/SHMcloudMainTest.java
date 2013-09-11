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
import java.io.IOException;
import static org.junit.Assert.assertTrue;

import org.freeeed.main.PlatformUtil;
import org.freeeed.main.SHMcloudMain;
import org.freeeed.main.WindowsReduce;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.Project;
import org.junit.*;

public class SHMcloudMainTest {

    public SHMcloudMainTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMain() {
        System.out.println("FreeEedMainTest.testMain");
        String[] args = new String[2];
        args[0] = "-param_file";
        String platform = PlatformUtil.getPlatform().toString().toLowerCase();
        // this will test local environment
        args[1] = "sample_freeeed_" + platform + ".project";
        // MK testing Hadoop env
        // args[1] = "enron_12_ec2.project";
        // delete output, so that the test should run
        Project project = new Project().loadFromFile(new File(args[1]));
        Project.setProject(project);
        try {
            if (new File(project.getOutputDir()).exists()) {
                Files.deleteRecursively(new File(project.getOutputDir()));
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.WINDOWS) {
            WindowsReduce.reinit();
        }
        SHMcloudMain.main(args);
        // TODO - do more tests        
        String outputSuccess = project.getResultsDir() + "/_SUCCESS";
        assertTrue(new File(outputSuccess).exists());
        String metadataFile = project.getResultsDir() + File.separator;
        if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.WINDOWS) {
            metadataFile += "metadata.txt";
        } else {
            metadataFile += "part-r-00000";
        }
        assertTrue(new File(metadataFile).exists());
        try {
            int resultCount = FreeEedUtil.countLines(metadataFile);
            System.out.println("FreeEedMainTest.testMain: resultCount = " + resultCount);
            // TODO find out why the results are different between Linux and Windows,
            // maybe it's just the way we count the rows?
            if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.WINDOWS) {
                assertTrue("resultCount == 2306", resultCount == 2306);
            } 
            if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.LINUX) {
                assertTrue("resultCount == 2305", resultCount == 2305);
            } 
            if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.MACOSX) {
                assertTrue("resultCount == 2306", resultCount == 2305);
            }             
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
