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
                
        assertEquals(metadata.getMessageFrom(), "\"Denton  Rhonda L.\" <Rhonda.Denton@ENRON.com>");
        
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
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/word/AdminContracts.doc", metadata, "AdminContracts.doc");
        
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
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/spreadsheet/tti.xls", metadata, "tti.xls");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("Affiliation Agreements"));
        assertTrue(body.contains("Agreement with foreign governmental bodies"));
        assertTrue(body.contains("TTI Division Head or Center Director"));
        assertTrue(body.contains("All contracts for goods or services must be in compliance with System Regulation 25.99.02 and the System Procurement Code.  All purchases shall comply with"));
        assertTrue(body.contains("(Personal property requires Board of Regents acceptance.  Real property"));
    }
    
    @Test
    public void testParsePDF() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/pdf/BPR4PKU.pdf", metadata, "BPR4PKU.pdf");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("China Research Lab"));
        assertTrue(body.contains("Business Process Reengineering"));
        assertTrue(body.contains("Wang Feng Chun"));
        assertTrue(body.contains("IBM Confidential"));
        assertTrue(body.contains("In the 1990s, Michael Hammer and James Champy introduced their"));
        assertTrue(body.contains("Organize around outcomes, not tasks"));
    }
    
    @Test
    public void testParsePPT() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/presentation/bids.ppt", metadata, "bids.ppt");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("The State of Rhode Island"));
        assertTrue(body.contains("Department of Transportation"));
        assertTrue(body.contains("ALL BIDDERS  MUST REGISTER ONLINE"));
        assertTrue(body.contains("As a courtesy, RIDOT will also distribute any additional Engineering plans."));
        assertTrue(body.contains("John  J. Lynch  ext. 4405"));
        assertTrue(body.contains("A bid bond payable to the State of Rhode Island for"));
    }
    
    @Test
    public void testParseHtml() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/html/ibm_letter.html", metadata, "ibm_letter.html");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("isham research"));
        assertTrue(body.contains("IBM Confidential"));
        assertTrue(body.contains("This is a poor facsimilie - it's only really here to register the keywords with Google - but you can go to IBM's original PDF by clicking"));
        assertTrue(body.contains("Note, in passing, that the signature is illegible and not accompanied by the signatory's name - as would be only courteous"));
        assertTrue(body.contains("IBM United Kingdom Ltd"));
        assertTrue(body.contains("Fax +44 (0) 20 7928 4464"));
    }
    
    @Test
    public void testParseText() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse("test-data/02-loose-files/docs/text/document.txt", metadata, "document.txt");
        
        String body = metadata.getDocumentText();
        assertTrue(body.contains("SECTION B"));
        assertTrue(body.contains("SUPPLIES OR SERVICES AND PRICES/COST"));
        assertTrue(body.contains("B.1 General"));
        assertTrue(body.contains("This contract is titled the Veterans Technology Services Governmentwide Acquisition Contract (VETS GWAC or VETS) and is available for use by both Federal Civilian Agencies and the Department of Defense by virtue of the GSA’s Executive Agent Designation from the Office of Management and Budget. It has a base period of five years and one five-year option for a total of ten contract years (actual calendar dates will be set beginning with the date of the notice to proceed)."));
        assertTrue(body.contains("VETS GWAC consists of a number of indefinite-delivery, indefinite-quantity (ID/IQ) contracts designed to provide Federal Government information technology (IT) services and solutions primarily consisting of IT services."));
        assertTrue(body.contains("The contracts are solution-based. VETS GWAC contractors are free to propose the best solution to the specific task order requirement provided each order consists principally of IT services. Unless excepted (see FAR 16.505(b)(2)), each task order will be competed under the fair opportunity competitive procedures. The Fair Opportunity competitive procedures will maintain an ongoing competitive environment throughout the life of the contracts."));
    }
}
