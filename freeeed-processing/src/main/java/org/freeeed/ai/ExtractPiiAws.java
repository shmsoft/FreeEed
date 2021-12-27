package org.freeeed.ai;

import okhttp3.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * https://docs.aws.amazon.com/comprehend/latest/dg/get-started-api-pii.html
 */

public class ExtractPiiAws {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPiiAws.class);
    public String extractPiiAsString(String data) {
        return "AWS-extractoed PII";
    }
}
