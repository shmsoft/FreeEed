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

import org.freeeed.util.PlatformUtil;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;

import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.junit.*;

public class FreeEedSmallTest {

    @Test
    public void testMain() throws IOException {
        String[] args = new String[2];
        args[0] = "-param_file";
        args[1] = "projects/small_test.project";
        // delete output, so that the test should run
        Project project = Project.loadFromFile(new File(args[1]));
        if (new File(project.getOutputDir()).exists()) {
            FileUtils.deleteDirectory(new File(project.getOutputDir()));
            //Files.deleteRecursively(new File(project.getOutputDir()));
        }
        if (PlatformUtil.isWindows()) {
            WindowsReduce.reinit();
        }
        FreeEedMain.main(args);
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
            //int resultCount = Files.readLines(new File(metadataFile), Charset.defaultCharset()).size();
            int resultCount = Util.countLines(metadataFile);
            System.out.println("resultCount = " + resultCount);
            assertTrue("resultCount == 11, really, " + resultCount, resultCount == 11);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
