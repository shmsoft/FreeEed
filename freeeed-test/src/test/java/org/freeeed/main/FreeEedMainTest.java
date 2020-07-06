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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;

public class FreeEedMainTest {

    private static final Logger logger = LoggerFactory.getLogger(FreeEedMainTest.class);
    private static final String projectString
            = "project-file-path=/home/mark/projects/SHMcloud/sample_freeeed_linux.project\n"
            + "project-code=0002\n"
            + "staging-dir=test-output/staging\n"
            + "output-dir=test-output/output\n"
            + "file-system=local\n"
            + "solr_endpoint=http\\://localhost\\:8983\n"
            + "files-per-zip-staging=50\n"
            + "project-file-name=sample_freeeed_linux.project\n"
            + "input=test-data/01-one-time-test,test-data/02-loose-files,test-data/03-enron-pst\n"
            + "field-separator=pipe\n"
            + "metadata=standard\n"
            + "custodian=c1,c2,c3\n"
            + "run=\n"
            + "culling=\n"
            + "create-pdf=false\n"
            + "lucene_fs_index_enabled=false\n"
            + "remove-system-files=\n"
            + "send_index_solr_enabled=false\n"
            + "stage=true\n"
            + "gigs-per-zip-staging=0.1\n"
            + "output-dir-hadoop=freeeed-output/0002/output/run-120805-173957/results\n"
            + "process-where=local\n"
            + "ocr_enabled=false\n"
            + "project-name=My sample project\n"
            + "data_source=0\n"
            + "process_timeout_sec=300\n";

    @BeforeClass
    public static void setUpClass() {
        OsUtil.systemCheck();
    }

    // TODO, we are not longer based on *project files, so need to
    // redo this test
    @Test
    public void testMain() throws IOException {
        {
            System.out.println("FreeEedMainTest.testMain");
            String[] args = new String[2];
            args[0] = "-param_file";
            //String platform = ("" + OsUtil.getOs()).toLowerCase();
            // this will test local environment
            args[1] = "output/freeeed.project";
            FileUtils.write(new File(args[1]), projectString, StandardCharsets.UTF_8);
            // MK testing Hadoop env
            // args[1] = "enron_12_ec2.project";
            // delete output, so that the test should run
            Project project = Project.loadFromFile(new File(args[1]));
            try {
                if (new File(project.getOutputDir()).exists()) {
                    FileUtils.deleteDirectory(new File(project.getOutputDir()));
                    //Files.deleteRecursively(new File(project.getOutputDir()));
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
            FreeEedMain.main(args);
            // TODO - do more tests        
            String outputSuccess = project.getResultsDir();
            assertTrue(new File(outputSuccess).exists());
            String metadataFile = project.getResultsDir() + File.separator;
            int expectedResultCount = 0;
            if (OsUtil.isWindows()) {
                metadataFile += "metadata.csv";
                expectedResultCount = 2310;
            } else {
                metadataFile += "metadata.csv";
                expectedResultCount = 2478;
            }
            assertTrue(new File(metadataFile).exists());
            try {
                int resultCount = Util.countLines(metadataFile);
                System.out.println("FreeEedMainTest.testMain: resultCount = " + resultCount);
                assertTrue("resultCount == 2478, really, " + resultCount, resultCount == expectedResultCount);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }
    // may be needed later
    private String enronProjectString = "#SHMcloud Project\n"
            + "#Tue Dec 25 14:08:48 CST 2012\n"
            + "project-file-path=/home/mark/projects/SHMcloud/enron_12_ec2.project\n"
            + "project-code=1002\n"
            + "file-system=local\n"
            + "solr_endpoint=http\\://localhost\\:8983\n"
            + "files-per-zip-staging=100\n"
            + "project-file-name=enron_12_ec2.project\n"
            + "input=s3\\://shmsoft/enron/edrm-enron-v2_arora-h_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_pereira-s_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_south-s_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_rapp-b_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_harris-s_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_slinger-r_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_panus-s_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_bailey-s_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_dean-c2_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_sanchez-m_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_king-j_pst.zip,s3\\://shmsoft/enron/edrm-enron-v2_hendrickson-s_pst.zip\n"
            + "field-separator=pipe\n"
            + "metadata=all\n"
            + "custodian=,,,,,,,,,,,\n"
            + "run=\n"
            + "culling=\\r\\r\n"
            + "ocr_max_images_per_pdf=10\n"
            + "staging_dir=test-output/staging\n"
            + "create-pdf=false\n"
            + "output_dir=test-output/output\n"
            + "s3bucket=s3\\://shmsoft\n"
            + "lucene_fs_index_enabled=false\n"
            + "remove-system-files=true\n"
            + "new-project-name=New project\n"
            + "http=//shmsoft.s3.amazonaws.com/enron/\n"
            + "send_index_solr_enabled=true\n"
            + "gigs-per-zip-staging=1.0\n"
            + "process-where=local\n"
            + "ocr_enabled=true\n"
            + "project-name=Enron 12";

}
