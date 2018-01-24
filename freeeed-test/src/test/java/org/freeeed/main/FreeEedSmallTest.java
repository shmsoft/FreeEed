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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.junit.*;
import static org.junit.Assert.assertTrue;

public class FreeEedSmallTest {

    @Test
    public void testMain() throws IOException {
        String[] args = new String[2];
        args[0] = "-param_file";
        args[1] = "output/small_test.project";
        FileUtils.write(new File(args[1]), PROJECT_AS_STRING, StandardCharsets.UTF_8);
        // delete output, so that the test should run
        Project project = Project.loadFromFile(new File(args[1]));
        if (new File(project.getOutputDir()).exists()) {
            FileUtils.deleteDirectory(new File(project.getOutputDir()));
        }
        FreeEedMain.main(args);
        // TODO - do more tests        
        String outputSuccess = project.getResultsDir() + "/_SUCCESS";
        assertTrue(new File(outputSuccess).exists());
        String metadataFile = project.getResultsDir() + File.separator;
        if (OsUtil.isWindows()) {
            metadataFile += "metadata.csv";
        } else {
            metadataFile += "metadata.csv";
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
    private static final String PROJECT_AS_STRING
            = "project-file-path=small_test.project\n"
            + "project-code=0009\n"
            + "metadata-collection=standard\n"
            + "#text-in-metadata=\n"
            + "staging-dir=test-output/staging\n"
            + "output-dir=test-output/output\n"
            + "file-system=local\n"
            + "files-per-zip-staging=50\n"
            + "project-file-name=small_test.project\n"
            + "input=../test-data/01-one-time-test,../test-data/01-one-time-test_1\n"
            + "field-separator=pipe\n"
            + "metadata=standard\n"
            + "custodian=c1,c2\n"
            + "run=\n"
            + "culling=\n"
            + "load-format=csv\n"
            + "stage=true\n"
            + "process-where=local\n"
            + "project-name=My small sample project\n"
            + "data_source=0\n"
            + "gigs-per-zip-staging=.1";
}
