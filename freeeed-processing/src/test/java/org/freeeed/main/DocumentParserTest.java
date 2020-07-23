package org.freeeed.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class DocumentParserTest {

    @Test
    public void testParseEml() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse(new DiscoveryFile("test-data/02-loose-files/docs/eml/1.eml", "1.eml"), metadata);

        assertEquals("\"Denton  Rhonda L.\" <Rhonda.Denton@ENRON.com>", metadata.getMessageFrom());

        assertEquals("Murphy  Melissa <Melissa.Murphy@ENRON.com> , Bailey  Susan <Susan.Bailey@ENRON.com>", metadata.getMessageTo());
        assertEquals("Anderson  Diane <Diane.Anderson@ENRON.com> , Cason  Sharen <Sharen.Cason@ENRON.com>", metadata.getMessageCC());

        String subject = metadata.getMessageSubject();
        assertNotNull(subject);
        assertTrue(subject.contains("RE:  TOP TEN counterparties (for ENA) - Non-Terminated, in-the-money positions"));

        assertEquals("2002-02-01T15:35:50.0+00:00", metadata.getMessageDateReceived());
        assertEquals("15:35:50", metadata.getMessageTimeReceived());
        assertEquals("2002-02-01T15:35:50Z", metadata.getMessageCreationDate());
        assertEquals("<5ACD5A6F6BD6874D81DA91C6A7F577652F6C45@NAHOU-MSMBX07V.corp.enron.com>", metadata.getMessageId());

        String body = metadata.getDocumentText();
        assertTrue(body.contains("Here are the reports we prepared.  We only trade with 5 of the listed entities.  The reports are done individually by CP b/c we used our canned report (Flowing Deals)  instead of report writer.  If you need any other information or need the information manipulated (smoking gun) in another way, let us know.  We can make changes as necessary."));
        assertTrue(body.contains("Set forth below is the list we discussed.  First determine if there is a physical power relationship with each counterparty listed.  If so, please furnish confirms or a listing that evidence the following requests:"));
        assertTrue(body.contains("Please send me the names of the 10 counterparties that we are evaluating.  Thanks!"));
    }

    @Test
    public void testParseWord() {
        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/word/AdminContracts.doc", "AdminContracts.doc"),
                metadata);

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
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/spreadsheet/tti.xls", "tti.xls"), metadata);

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
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/pdf/BPR4PKU.pdf", "BPR4PKU.pdf"), metadata);

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
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/presentation/bids.ppt", "bids.ppt"), metadata);

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
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/html/ibm_letter.html", "ibm_letter.html"), metadata);

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
        DocumentParser.getInstance().parse(
                new DiscoveryFile("test-data/02-loose-files/docs/text/document.txt", "document.txt"), metadata);

        String body = metadata.getDocumentText();
        assertTrue(body.contains("SECTION B"));
        assertTrue(body.contains("SUPPLIES OR SERVICES AND PRICES/COST"));
        assertTrue(body.contains("B.1 General"));
        assertTrue(body.contains("This contract is titled the Veterans Technology Services Governmentwide Acquisition Contract (VETS GWAC or VETS) and is available for use by both Federal Civilian Agencies and the Department of Defense by virtue of the GSA’s Executive Agent Designation from the Office of Management and Budget. It has a base period of five years and one five-year option for a total of ten contract years (actual calendar dates will be set beginning with the date of the notice to proceed)."));
        assertTrue(body.contains("VETS GWAC consists of a number of indefinite-delivery, indefinite-quantity (ID/IQ) contracts designed to provide Federal Government information technology (IT) services and solutions primarily consisting of IT services."));
        assertTrue(body.contains("The contracts are solution-based. VETS GWAC contractors are free to propose the best solution to the specific task order requirement provided each order consists principally of IT services. Unless excepted (see FAR 16.505(b)(2)), each task order will be competed under the fair opportunity competitive procedures. The Fair Opportunity competitive procedures will maintain an ongoing competitive environment throughout the life of the contracts."));
    }

    @Test
    public void testInternational() {
        File dir = new File("test-data/02-loose-files/docs/international");
        String file = dir.list()[0];
        String fileName = dir + File.separator + file;

        DocumentMetadata metadata = new DocumentMetadata();
        DocumentParser.getInstance().parse(new DiscoveryFile(fileName, "cartilha_educação_financeira.pdf"), metadata);

        String body = metadata.getDocumentText();
        assertTrue(body.contains("não pode cobrar tarifa pela"));
        assertTrue(body.contains("As consultas pela internet são"));

        assertTrue(body.contains("muito bem o seu uso, pois dependendo como essa"));
        assertTrue(body.contains("Ler atentamente o contrato é necessário para conhece"));
    }
    
    @Test
    public void testDocumentWithOLEEmbedded() {
    	DocumentMetadata metadata = new DocumentMetadata();
    	DocumentParser.getInstance().parse(new DiscoveryFile("test-data/02-loose-files/docs/word/word_with_embedded_objects_xls_tables.docx", "word_with_embedded_objects_xls_tables.docx"), metadata);
    	String body = metadata.getDocumentText();

    	assertTrue(body.contains("First table sheet 1"));
    	assertTrue(body.contains("First table B2"));
    	assertTrue(body.contains("First table C5"));
    	assertTrue(body.contains("First table F10"));
    	assertTrue(body.contains("First table sheet 2"));
    	assertTrue(body.contains("First table sheet 2 - A4"));
    	assertTrue(body.contains("First table sheet 2 - C6"));
    	
    	assertTrue(body.contains("Second table sheet 1"));
    	assertTrue(body.contains("Second table A1"));
    	assertTrue(body.contains("Second table A2"));
    	assertTrue(body.contains("Second table B3"));
    	
    	assertTrue(body.contains("Second table sheet 2"));
    	assertTrue(body.contains("Second table sheet 2 - C7"));
    }
    
}
