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
package org.freeeed.es;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class EsClientTest {
    
    public EsClientTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of open method, of class EsClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testOpen() throws Exception {
        System.out.println("open");
        // this host is the trial instance of ElasticSearch on AWS
        String host = "cd563a0b2c895eba8facac558b3e887a.us-east-1.aws.found.io";
        EsClient instance = new EsClient();
        instance.setHost(host);
        instance.open();
        assertNotNull(instance.getClient());
    }    
}
