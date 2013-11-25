package org.freeeed.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
    public void testParseEml() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/eml/215.eml", metadata, "215.eml");
        
        String from = metadata.get(DocumentMetadataKeys.MESSAGE_FROM);
        assertEquals("\"Denton  Rhonda L.\" <Rhonda.Denton@ENRON.com>", from);
        
        String to = metadata.get(DocumentMetadataKeys.MESSAGE_TO);
        assertEquals("Murphy  Melissa <Melissa.Murphy@ENRON.com> , Bailey  Susan <Susan.Bailey@ENRON.com>", to);
        
        String cc = metadata.get(DocumentMetadataKeys.MESSAGE_CC);
        assertEquals("Anderson  Diane <Diane.Anderson@ENRON.com> , Cason  Sharen <Sharen.Cason@ENRON.com>", cc);
        
        String subject = metadata.get(DocumentMetadataKeys.SUBJECT);
        assertNotNull(subject);
        assertTrue(subject.contains("RE:  TOP TEN counterparties (for ENA) - Non-Terminated, in-the-money positions"));
        
        String dateReceived = metadata.get(DocumentMetadataKeys.DATE_RECEIVED);
        assertEquals("20020201", dateReceived);
        
        String timeReceived = metadata.get(DocumentMetadataKeys.TIME_RECEIVED);
        assertEquals("17:35", timeReceived);
        
        String date =  metadata.get(DocumentMetadataKeys.MESSAGE_DATE);
        assertEquals("2002-02-01T15:35:50Z", date);
        
        String body = metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
        
        System.out.println("asv: " + body);
    }
}
