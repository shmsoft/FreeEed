package org.freeeed.form;

import org.apache.commons.io.FileUtils;
import org.freeeed.main.FreeEedMain;
import org.freeeed.util.LogFactory;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

public class FormExtractorTest {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(FormExtractorTest.class.getName());

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
        String text = FileUtils.readFileToString(new File("test-data/08-forms/18-clean.txt"),"UTF-8");
        FormExtractor instance = new FormExtractor();
        instance.extract(text);
        assertTrue(true);
    }
}