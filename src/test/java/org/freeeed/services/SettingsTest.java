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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.services;

import java.util.List;

import org.freeeed.services.Settings;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class SettingsTest {
    
    public SettingsTest() {
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
     * Test of loadFromString method, of class Settings.
     */
    @Test
    public void testLoadFromString() {
        System.out.println("loadFromString");
        Settings.load();
        Settings settings = Settings.getSettings();
        String str = settings.toString();
        Settings result = Settings.loadFromString(str);
        assertEquals(settings, result);
    }
}
