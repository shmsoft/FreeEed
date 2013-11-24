package org.freeeed.main;

import org.apache.tika.metadata.Metadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DocumentParserTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testParse() {
        Metadata metadata = new Metadata();
        DocumentParser.getInstance().parse("test-data/01-one-time-test/215.eml", metadata, "abc");
        System.out.println("asasdas");
        System.out.println(metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT));
    }

}
