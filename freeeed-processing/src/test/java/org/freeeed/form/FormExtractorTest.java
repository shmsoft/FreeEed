package org.freeeed.form;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FormExtractorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(org.freeeed.ai.SummarizeTextTest.class);

    @Test
    public void extractText() {
        String text = "ARREST DATE: 07/28/2000 AGENCY: NEVADA DMVPS PAROLE AND PROBATION " +
                "Some other text here " +
                "ARREST DATE: 08/15/2002 AGENCY: CALIFORNIA DMV PAROLE AND PROBATION";

        FormExtractor instance = new FormExtractor();
        instance.extract(text);
        assertTrue(true);
    }
    @Test
    public void extractTextFromForm() throws IOException {
        String text = FileUtils.readFileToString(new File("test-data/forms/18-clean.txt"),"UTF-8");
        FormExtractor instance = new FormExtractor();
        instance.extract(text);
        assertTrue(true);
    }
}