package org.freeeed.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.comprehend.ComprehendClient;
import software.amazon.awssdk.services.comprehend.model.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class ExtractPiiAws {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractPiiAws.class);

    private final String awsAccessKeyId;
    private final String awsSecretAccessKey;
    private final Region awsRegion;
    private final HashMap<String, String> piiInDoc = new HashMap<>();
    private ComprehendClient comClient;
    private int charLimit = 4000;

    public ExtractPiiAws(String awsAccessKeyId, String awsSecretAccessKey, Region awsRegion) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.awsRegion = awsRegion;
        initClient();
    }

    public ExtractPiiAws(String awsAccessKeyId, String awsSecretAccessKey) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
        this.awsRegion = Region.US_EAST_1;
        initClient();
    }

    public void initClient() {

        comClient = ComprehendClient.builder()
                .region(awsRegion)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretAccessKey)))
                .build();
    }

    private void updateHashMap(PiiEntityType piiType, String value) {
        String key = piiType.toString();
        if (piiInDoc.get(key) != null) {
            value = piiInDoc.get(key) + ", " + value;
        }
        piiInDoc.put(key, value);
    }

    public HashMap<String, String> extractPIIBySegment(String document) {
        HashMap<String, String> accumulator = new HashMap<String, String>();
        TextSplitter splitter = new TextSplitter(charLimit);

        List<String> segments = splitter.splitBySentenceWithLimit(document);

        for (String segment: segments) {
            HashMap<String, String> partialMap = extractPII(segment);
            accumulator.putAll(partialMap);
        }
        return accumulator;
    }
    private HashMap<String, String> extractPII(String document) {
        // TODO debugging
        System.out.println(document.length());
        if (document.length() > charLimit) {
            System.out.println(document.length() + document);
        }
        DetectPiiEntitiesRequest detectPiiRequest = DetectPiiEntitiesRequest.builder()
                .text(document)
                .languageCode("en")
                .build();
        DetectPiiEntitiesResponse detectEntitiesResult = null;
        try {
            detectEntitiesResult = comClient.detectPiiEntities(detectPiiRequest);
        } catch (TextSizeLimitExceededException e) {
            LOGGER.error("AWS PII problem", e);
        }

        if (detectEntitiesResult == null) {
            piiInDoc.put("Error", "Text too long");
            return piiInDoc;
        }
        Iterator<PiiEntity> lanIterator = detectEntitiesResult.entities().iterator();

        while (lanIterator.hasNext()) {
            PiiEntity entity = lanIterator.next();
            String docText = document.substring(entity.beginOffset(), entity.endOffset());
            updateHashMap(entity.type(), docText);
        }

        return piiInDoc;
    }
}