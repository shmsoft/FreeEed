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

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.junit.*;

public class FreeEedMainTest {

    public FreeEedMainTest() {
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
        String platform = ("" + PlatformUtil.getOs()).toLowerCase();
        // this will test local environment
        args[1] = "projects/sample_freeeed_" + platform + ".project";
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
        if (PlatformUtil.isWindows()) {
            WindowsReduce.reinit();
        }
        SHMcloudMain.main(args);
        // TODO - do more tests        
        String outputSuccess = project.getResultsDir() + "/_SUCCESS";
        assertTrue(new File(outputSuccess).exists());
        String metadataFile = project.getResultsDir() + File.separator;
        if (PlatformUtil.isWindows()) {
            metadataFile += "metadata.txt";
        } else {
            metadataFile += "part-r-00000";
        }
        assertTrue(new File(metadataFile).exists());
        try {
            int resultCount = Util.countLines(metadataFile);
            System.out.println("FreeEedMainTest.testMain: resultCount = " + resultCount);
            // TODO find out why the results are different between Linux and Windows,
            // maybe it's just the way we count the rows?
            if (PlatformUtil.isWindows()) {
                assertTrue("resultCount == 2310", resultCount == 2310);
            } 
            else {
                assertTrue("resultCount == 2308", resultCount == 2308);
            } 
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
