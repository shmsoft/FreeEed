package org.freeeed.main;

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
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/eml/1.eml", metadata, "abc");
        assertEquals(metadata.getFrom(),"Denton  Rhonda L. <Rhonda.Denton@ENRON.com>");        
    }
}
