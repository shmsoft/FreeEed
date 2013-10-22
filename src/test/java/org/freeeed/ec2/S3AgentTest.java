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
import java.util.Date;
import static org.junit.Assert.assertEquals;

import org.freeeed.ec2.S3Agent;
import org.freeeed.services.Settings;
import org.junit.*;

/**
 *
 * @author mark
 */
public class S3AgentTest {
    
    public S3AgentTest() {
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


    /**
     * Test of getFileFromS3 method, of class S3Agent.
     */
    //@Test
    public void testGetFileFromS3() {
        System.out.println("getFileFromS3");
        String fileKey = "s3://shmsoft/1002/output/run-120520-224525/staging/input00001_Enron.zip";
        String outputFile = "/mnt/tmp/temp.zip";
        S3Agent instance = new S3Agent();
        boolean expResult = true;
        Settings.load();
        Date start = new Date();
        boolean result = instance.getStagedFileFromS3(fileKey, outputFile);
        Date finish = new Date();
        long duration = (finish.getTime() - start.getTime()) / 1000;
        System.out.println(new File(outputFile).length() + "  bytes written in " + duration + " seconds");
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
