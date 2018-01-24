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
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
     *
     * @throws Exception
     */
    @Test
    public void testLoadMode() throws Exception {
        System.out.println("testLoadMode");
        DbLocalUtils.loadMode();
        Mode mode = Mode.getInstance();
        assertNotNull(mode.getRunMode());
    }

    /**
     * Test of initial setup for settings
     *
     * @throws Exception
     */
    @Test
    public void testSettingsInitValues() throws Exception {
        System.out.println("testInitValues");
        DbLocalUtils.loadSettings();
        Settings settings = Settings.getSettings();
        // check that some known settings are indeed there
        assertNotNull(settings.getManualPage());
    }

    /**
     * Test of saving the settings
     *
     * @throws Exception
     */
    @Test
    public void testSaveSettings() throws Exception {        
        System.out.println("testSaveSettings");
        // for use cases where the table was deleted, it needs to be created first
        DbLocalUtils.createSettingsTable();
        DbLocalUtils.loadSettings();
        Settings settings = Settings.getSettings();
        String testStr = Math.random() + "";
        settings.put("test", testStr);
        DbLocalUtils.saveSettings();
        settings.put("test", "reset");
        DbLocalUtils.loadSettings();
        assertEquals(testStr, settings.get("test"));
        // now set it back to OK, just to be nice
        settings.put("test", "OK");
        DbLocalUtils.saveSettings();
    }

}
