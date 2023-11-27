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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark
 */
public class S3UploaderTest {

    private static final Logger logger = LoggerFactory.getLogger(S3UploaderTest.class);

    @Test
    public void testUploadFile() {
        String bucketName = "freeeed.org";
        String keyName = "temp/small.zip";
        String filePath = "test-data/05-processes-small/small.zip";
        S3Uploader s3Uploader = new S3Uploader();
        s3Uploader.uploadFile(bucketName, keyName, filePath);
    }
}
