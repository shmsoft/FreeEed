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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author mark
 */
public class SettingsTest {

    /**
     * Test load mappings.
     */
    @Test
    public void testLoad() {
        String settingsStr
                = "instance-type=c1.medium" + "\n"
                + "process-where=ec2" + "\n"
                + "availability-zone=us-east-1a" + "\n"
                + "cluster-timeout=5" + "\n"
                + "manual-page=https://github.com/markkerzner/FreeEed/wiki" + "\n"
                + "skip=0" + "\n"
                + "# Release" + "\n"
                + "download-link=https://s3.amazonaws.com/shmsoft/FreeEed.zip" + "\n"
                + "# Release candidate RC" + "\n"
                + "# download-link=https://s3.amazonaws.com/shmsoft/SHMcloudRC.zip" + "\n"
                + "items-per-mapper=5000" + "\n"
                + "bytes-per-mapper=250000000" + "\n"
                + "#load_balance=" + "\n"
                + "ami=ami-9be68af2";
        Settings settings = Settings.loadFromString(settingsStr);
        assertTrue(settings.getAccessKeyId().isEmpty());
        assertEquals(settings.getAvailabilityZone(), "us-east-1a");
        assertEquals(settings.getBytesPerMapper(), 250000000L);
        assertEquals(settings.getClusterAmi(), "ami-9be68af2");
        assertEquals(settings.getClusterSize(), 1);
        assertEquals(settings.getClusterTimeoutMin(), 5);
        assertTrue(settings.getCurrentDir().isEmpty());
        assertEquals(settings.getEnv(), "ec2");
        assertTrue(settings.getExternalProssingEndpoint().isEmpty());
        assertEquals(settings.getInstanceType(), "c1.medium");
        assertEquals(settings.getItemsPerMapper(), 5000);
        assertTrue(settings.getKeyPair().isEmpty());
        assertEquals(settings.getManualPage(), "https://github.com/markkerzner/FreeEed/wiki");
        assertTrue(settings.getProjectBucket().isEmpty());
        assertTrue(settings.getProperty("wild-non-existent-key").isEmpty());
        assertTrue(settings.getSecretAccessKey().isEmpty());
        assertTrue(settings.getSecurityGroup().isEmpty());
    }

    @Test
    public void testSettingsPresent() throws Exception {
        Settings.load();
        assertTrue(Settings.getSettings() != null);
    }
}
