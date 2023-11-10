/**
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

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.freeeed.db.DbLocalUtils;
import org.freeeed.util.LogFactory;
import org.slf4j.LoggerFactory;

public class ContentTypeMapping {

    private final static Logger LOGGER = LogFactory.getLogger(ContentTypeMapping.class.getName());
    private final Map<String, String> mapping;

    public ContentTypeMapping() {
        try {
            mapping = DbLocalUtils.loadContentTypeMapping();
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize content type mapping", e);
        }
    }

    public String getFileType(String contentType) {
        contentType = trimCharset(contentType);
        if (StringUtils.isEmpty(contentType)) {
            LOGGER.warning("Empty content type detected");
            return null;
        }
        String fileType = mapping.get(contentType);
        if (StringUtils.isEmpty(fileType)) {
            LOGGER.warning("Not found file type for content type: " + contentType);
        }
        return fileType;
    }

    private String trimCharset(String contentType) {
        int p = contentType.indexOf("; charset");
        if (p >= 0) {
            return contentType.substring(0, p);
        }
        return contentType;
    }

}
