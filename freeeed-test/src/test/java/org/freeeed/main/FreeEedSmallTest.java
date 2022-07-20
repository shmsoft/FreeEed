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

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.freeeed.services.Util;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FreeEedSmallTest {
    //@Test
    public void testMain() throws IOException {
        String[] args = new String[2];
        args[0] = "-param_file";
        args[1] = "test-data/small_test.project";
        // FileUtils.write(new File(args[1]), projectString, StandardCharsets.UTF_8);
        // delete output, so that the test should run
        Project project = Project.loadFromFile(new File(args[1]));
        if (new File(project.getOutputDir()).exists()) {
            FileUtils.deleteDirectory(new File(project.getOutputDir()));
        }
        FreeEedMain.main(args);
        String outputSuccess = project.getResultsDir();
        assertTrue(new File(outputSuccess).exists());
        String metadataFile = project.getResultsDir() + File.separator;
        int expectedResultCount = 11;
        metadataFile += "metadata1.csv";
        assertTrue(new File(metadataFile).exists());

        try {
            int resultCount = Util.countLines(metadataFile);
            System.out.println("FreeEedMainTest.testMain: resultCount = " + resultCount);
            assertTrue("Expected resultCount " + expectedResultCount + ", really, " + resultCount, resultCount == expectedResultCount);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
