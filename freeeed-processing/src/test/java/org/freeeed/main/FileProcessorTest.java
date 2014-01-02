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

import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;

import org.apache.hadoop.io.MD5Hash;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.Util;

/**
 *
 * Class FileProcessorTest.
 *
 * @author ilazarov
 *
 */
public class FileProcessorTest extends TestCase {

    public void testCreateKeyHash() throws IOException {
        DocumentMetadata metadata1 = new DocumentMetadata();

        metadata1.addField("Message-To", "ivan@example.com").
                addField("Message-From", "ivan2@example.com").
                addField("Message-Cc", "koce@example.com").
                addField("subject", "junit test 1");

        // TODO what are we testing here?
        MD5Hash hash1 = Util.createKeyHash(new File("test1.eml"), metadata1);
        MD5Hash hash2 = Util.createKeyHash(new File("test1.eml"), metadata1);
        assertEquals(hash1, hash2);

        Metadata metadata2 = new Metadata();

        metadata2.add("Message-To", "ivan@example.com");
        metadata2.add("Message-From", "ivan2@example.com");
        metadata2.add("Message-Cc", "koce@example.com");
        metadata2.add("subject", "junit test 2");

        MD5Hash hash3 = Util.createKeyHash(new File("test1.eml"), metadata2);
        assertFalse(hash1.equals(hash3));

        Metadata metadata3 = new Metadata();
        MD5Hash hash4 = Util.createKeyHash(
                new File("test-data/02-loose-files/docs/ocr/516.pdf"), metadata3);
        MD5Hash hash5 = Util.createKeyHash(
                new File("test-data/02-loose-files/docs/ocr/516.pdf"), metadata3);
        assertEquals(hash4, hash5);
    }
}
