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
package org.freeeed.util;

import org.freeeed.main.FreeEedMain;
import org.junit.Test;

/**
 *
 * @author Mark
 */
public class ZipUtilTest {

    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ZipUtilTest.class.getName());

    @Test
    public void testListFilesInZip() {
        String zipFile = "test-data/05-processes-small/small.zip";
        ZipUtil.listFilesInZip(zipFile);
    }
}
