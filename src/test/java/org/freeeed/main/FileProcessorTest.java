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


import java.io.IOException;

import junit.framework.TestCase;

import org.apache.hadoop.io.MD5Hash;
import org.apache.tika.metadata.Metadata;
import org.freeeed.main.FileProcessor;
import org.junit.After;
import org.junit.Before;

/**
 * 
 * Class FileProcessorTest.
 * 
 * @author ilazarov
 *
 */
public class FileProcessorTest extends TestCase {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    public void testCreateKeyHash() {
        
        try {
            Metadata md = new Metadata();
            
            md.add("Message-To", "ivan@example.com");
            md.add("Message-From", "ivan2@example.com");
            md.add("Message-Cc", "koce@example.com");
            md.add("subject", "junit test 1");
            
            MD5Hash hash1 = FileProcessor.createKeyHash("test1.eml", md, "test1.eml");
            MD5Hash hash2 = FileProcessor.createKeyHash("test1.eml", md, "test1.eml");
            assertEquals(hash1, hash2);
            
            Metadata md2 = new Metadata();
            
            md2.add("Message-To", "ivan@example.com");
            md2.add("Message-From", "ivan2@example.com");
            md2.add("Message-Cc", "koce@example.com");
            md2.add("subject", "junit test 2");
            
            MD5Hash hash3 = FileProcessor.createKeyHash("test1.eml", md2, "test1.eml");
            assertTrue(!hash1.equals(hash3));
            
            Metadata md3 = new Metadata();
            MD5Hash hash4 = FileProcessor.createKeyHash("test-data/02-loose-files/docs/ocr/516.pdf", md3, "516.pdf");
            MD5Hash hash5 = FileProcessor.createKeyHash("test-data/02-loose-files/docs/ocr/516.pdf", md3, "516.pdf");
            assertEquals(hash4, hash5);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("unexpected exception occured!");
        }
    }
}
