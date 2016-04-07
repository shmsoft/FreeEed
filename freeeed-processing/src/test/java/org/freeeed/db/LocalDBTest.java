/*
 * Copyright 2016 SHMsoft, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.db;

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
public class LocalDBTest {
    
    public LocalDBTest() {
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
     * Test of getInstance method, of class LocalDB.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        LocalDB result = LocalDB.getInstance();
        assertNotNull(result);        
    }

    /**
     * Test of isLocalMode method, of class LocalDB.
     */
    @Test
    public void testIsLocalMode() {
        System.out.println("isLocalMode");
        LocalDB instance = null;
        boolean expResult = false;
        boolean result = instance.isLocalMode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setLocalModel method, of class LocalDB.
     */
    @Test
    public void testSetLocalModel() {
        System.out.println("setLocalModel");
        boolean b = false;
        LocalDB instance = null;
        instance.setLocalModel(b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
