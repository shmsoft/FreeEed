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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ec2;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import static org.junit.Assert.assertEquals;

import org.freeeed.services.Settings;
import org.junit.*;
import org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class S3AgentTest {

    @Before
    public void setUp() {
        try {
            Settings.load();
        } catch (IllegalStateException e) { 
            Assert.fail("For this test to run, the 'settings.properties' should be present, \n"
                    + "and should contain the required Amazon keys");
        }
    }

    /**
     * Test of getFileFromS3 method, of class S3Agent.
     */
    @Test
    public void testGetFileFromS3() throws IOException {
        String fileKey = "s3://freeeed.org/enron/results/enron001.txt";
        File outputFile = File.createTempFile("s3download", "txt");
        outputFile.deleteOnExit();
        S3Agent instance = new S3Agent();
        boolean expResult = true;
        Date start = new Date();
        boolean result = instance.getStagedFileFromS3(fileKey, outputFile.getPath());
        Date finish = new Date();
        long duration = (finish.getTime() - start.getTime()) / 1000;
        System.out.println(outputFile.length() + "  bytes written in " + duration + " seconds");
        assertEquals(expResult, result);

    }

    /**
     * Test of getFileFromS3 method, of class S3Agent.
     */
    //@Test
    public void testGetTextFileFromS3() {
        Settings.load();
        System.out.println("testGetTextFileFromS3");
        String bucket = "shmsoft";
        String fileKey = "SHMcloud.update";
        S3Agent instance = new S3Agent();
        String result = instance.getTextFromS3(bucket, fileKey);
        System.out.println("Contents of " + fileKey + " is " + result);
    }
}
