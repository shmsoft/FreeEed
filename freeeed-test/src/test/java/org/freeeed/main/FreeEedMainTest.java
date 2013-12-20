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
//    private static final String PROJECT =
//    "project-code=0001" + "\n"
//staging-dir=test-output/staging
//output-dir=test-output/output
//file-system=local
//solr_endpoint=http\://localhost\:8983
//files-per-zip-staging=50
//project-file-name=sample_freeeed_linux.project
//ulling=
//input=test-data/01-one-time-test,test-data/02-loose-files,test-data/03-enron-pst
//field-separator=pipe
//metadata=standard
//custodian=c1,c2,c3
//run=
//culling=
//create-pdf=false
//skip=0
//lucene_fs_index_enabled=false
//remove-system-files=
//send_index_solr_enabled=false
//stage=
//gigs-per-zip-staging=0.1
//output-dir-hadoop=freeeed-output/0002/output/run-120805-173957/results
//process-where=local
//ocr_enabled=false
//project-name=My sample project


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
        Project project = Project.loadFromFile(new File(args[1]));
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
            } else {
                assertTrue("resultCount == 2308", resultCount == 2308);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
