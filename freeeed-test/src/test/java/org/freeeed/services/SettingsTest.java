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
package org.freeeed.services;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 * @author mark
 */
public class SettingsTest {

    @Test
    public void testSettingsPresent() {
        try {
            Settings settings = Settings.load();            
            assertFalse(settings.isEmpty());
            // comment this out if you don't have Amazon account
            if (settings.getAccessKeyId().isEmpty() || settings.getSecretAccessKey().isEmpty()) {
                fail("Without the Amazon keys the tests cannot run. \nIf you still want to play with the project -"
                        + " comment out this test, but expect incomplete testing.");
            }
            // end-of comment this out if you don't have Amazon account
        } catch (IllegalStateException e) {
            // a new programmer has not prepared his settings file. Tell him what to do.            
            fail("Your settings file is invalid or absent. "
                    + "The instructions to create it are found in the 'for_developers_only' file. Good luck!");
        }

    }
}
