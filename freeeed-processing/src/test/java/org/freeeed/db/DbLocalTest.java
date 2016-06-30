/*
 * Copyright SHMsoft, Inc. 
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

import org.freeeed.services.Mode;
import org.freeeed.services.Settings;
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
public class DbLocalTest {
    
    public DbLocalTest() {
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
     * Test of initial setup for mode
     * @throws java.lang.Exception
     */
    @Test
    public void testLoadMode() throws Exception {
        System.out.println("testLoadMode");
        DbLocal.getInstance().loadMode();
        Mode mode = Mode.getInstance();
        assertNotNull(mode.getRunMode());
    }
    /**
     * Test of initial setup for settings
     * @throws java.lang.Exception
     */
    @Test
    public void testInitValues() throws Exception {
        System.out.println("testInitValues");
        DbLocal.getInstance().loadSettings();
        Settings settings = Settings.getSettings();
        // check that some known settings are indeed there
        assertNotNull(settings.getManualPage());        
    }
    /**
     * Test of initial setup for settings
     * @throws java.lang.Exception
     */
    @Test
    public void testSaveSettings() throws Exception {
        System.out.println("testSaveSettings");
        DbLocal.getInstance().loadSettings();
        Settings settings = Settings.getSettings();
        // save a test setting to some randon string, then verify it
        settings.put("test", this);
        // now set it back to OK, just to be nice
    }
    
}
