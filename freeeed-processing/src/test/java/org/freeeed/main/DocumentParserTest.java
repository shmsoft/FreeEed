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
        DocumentParser.getInstance().parse("test-data/eml/1.eml", metadata, "1.eml");
        
        String from = metadata.getMessageFrom();
        assertEquals("\"Denton  Rhonda L.\" <Rhonda.Denton@ENRON.com>", from);
        
        String to = metadata.getMessageTo();
        assertEquals("Murphy  Melissa <Melissa.Murphy@ENRON.com> , Bailey  Susan <Susan.Bailey@ENRON.com>", to);
        
        String cc = metadata.getMessageCC();
        assertEquals("Anderson  Diane <Diane.Anderson@ENRON.com> , Cason  Sharen <Sharen.Cason@ENRON.com>", cc);
        
        String subject = metadata.getMessageSubject();
        assertNotNull(subject);
        assertTrue(subject.contains("RE:  TOP TEN counterparties (for ENA) - Non-Terminated, in-the-money positions"));
        
        String dateReceived = metadata.getMessageDateReceived();
        assertEquals("20020201", dateReceived);
        
        String timeReceived = metadata.getMessageTimeReceived();
        assertEquals("15:35", timeReceived);
        
        String date =  metadata.getMessageCreationDate();
        assertEquals("2002-02-01T15:35:50Z", date);
        
        String body = metadata.getDocumentText();
        
        assertTrue(body.contains("Here are the reports we prepared.  We only trade with 5 of the listed entities.  The reports are done individually by CP b/c we used our canned report (Flowing Deals)  instead of report writer.  If you need any other information or need the information manipulated (smoking gun) in another way, let us know.  We can make changes as necessary."));
        assertTrue(body.contains("Set forth below is the list we discussed.  First determine if there is a physical power relationship with each counterparty listed.  If so, please furnish confirms or a listing that evidence the following requests:"));
        assertTrue(body.contains("Please send me the names of the 10 counterparties that we are evaluating.  Thanks!"));
    }
    
    @Test
    public void testParseWord() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/word/AdminContracts.doc", metadata, "AdminContracts.doc");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("Contract administration is the management of a contract after the contract is executed, (in most cases, signed by both parties), and the vendor has been sent a purchase order or a contract for Professional Services (CPS)."));
        assertTrue(body.contains("Administration of a contract may be the responsibility of receiving, inspecting and accepting goods and paying the invoice. Or it may be management over a multi-million dollar project, from beginning to end. Contract administration may be done by one person or by a team of many."));
        assertTrue(body.contains("The following are steps to e taken in administering complex contracts that may add  to, delete from or differ from the University's standard Terms and Conditions:"));
        assertTrue(body.contains("Become familiar with the contract"));
        assertTrue(body.contains("(WF HD Purchasing Stuff/CFPS Stuff/2004/AdminContracts.doc Rev 10/06/04)"));
    }
    
    @Test
    public void testParseSpreadsheet() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/spreadsheet/tti.xls", metadata, "tti.xls");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("Affiliation Agreements"));
        assertTrue(body.contains("Agreement with foreign governmental bodies"));
        assertTrue(body.contains("TTI Division Head or Center Director"));
        assertTrue(body.contains("All contracts for goods or services must be in compliance with System Regulation 25.99.02 and the System Procurement Code.  All purchases shall comply with"));
        assertTrue(body.contains("(Personal property requires Board of Regents acceptance.  Real property"));
    }
}
