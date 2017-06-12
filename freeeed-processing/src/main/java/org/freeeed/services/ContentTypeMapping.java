package org.freeeed.services;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.freeeed.db.DbLocalUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentTypeMapping {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentTypeMapping.class);
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
            LOGGER.warn("Empty content type detected");
            return null;
        }
        String fileType = mapping.get(contentType);
        if (StringUtils.isEmpty(fileType)) {
            LOGGER.warn("Not found file type for content type: " + contentType);
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
