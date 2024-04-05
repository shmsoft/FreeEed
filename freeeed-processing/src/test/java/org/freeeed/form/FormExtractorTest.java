package org.freeeed.form;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;

public class FormExtractorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(org.freeeed.ai.SummarizeTextTest.class);

    @Test
    public void extractText() {
        FormExtractor instance = new FormExtractor();
        instance.extract();
        assertTrue(true);
    }
}