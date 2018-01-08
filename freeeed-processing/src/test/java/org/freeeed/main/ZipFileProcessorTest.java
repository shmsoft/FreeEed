package org.freeeed.main;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;
import org.freeeed.util.CsvMetadataParser;
import org.junit.After;
import org.junit.Before;

import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipFileProcessorTest {
    Logger logger = LoggerFactory.getLogger(ZipFileProcessorTest.class);
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
    
    //@Test
    // TODO redo following EmlFileProcessorTest
    public void testProcess() {
        logger.debug("ZipFileProcessorTest.testProcess");
        Project.getCurrentProject().setEnvironment(Project.ENV_LOCAL);
        Project.getCurrentProject().setProperty(ParameterProcessing.PROJECT_CODE, "test");
        Project.getCurrentProject().setCurrentCustodian("ivan");
        Project.getCurrentProject().setTextInMetadata(true);
        System.setProperty("os.name", "windows");
        
        ZipFileProcessor zipProcessor = new ZipFileProcessor("../test-data/zip/data.zip", null, null);
        try {
            FileUtils.deleteDirectory(new File("freeeed-output/test/output/123"));
            
            Stats.getInstance().setJobStarted(Project.getCurrentProject().getProjectName());
            zipProcessor.process(false, null);
            
            List<String> lines = Files.readLines(new File("freeeed-output/test/output/123/results/metadata.txt"), Charset.forName("UTF-8"));
            
            CsvMetadataParser parser = new CsvMetadataParser("\t");
            Map<String, Map<String, String>> data = parser.parseLines(lines);
            
            Map<String, String> docLine = data.get("AdminContracts.doc");
            assertNotNull(docLine);
            String body = docLine.get("text");
            assertTrue(body.contains("Contract administration is the management of a contract after the contract is executed, (in most cases, signed by both parties), and the vendor has been sent a purchase order or a contract for Professional Services (CPS)."));
            
            Map<String, String> pptLine = data.get("bids.ppt");
            assertNotNull(pptLine);
            body = pptLine.get("text");
            assertTrue(body.contains("As a courtesy, RIDOT will also distribute any additional Engineering plans."));
            
            Map<String, String> pdfLine = data.get("BPR4PKU.pdf");
            assertNotNull(pdfLine);
            body = pdfLine.get("text");
            assertTrue(body.contains("Business Process Reengineering"));
            
            Map<String, String> textLine = data.get("document.txt");
            assertNotNull(textLine);
            body = textLine.get("text");
            assertTrue(body.contains("This contract is titled the Veterans Technology Services Governmentwide Acquisition Contract"));
            
            Map<String, String> htmlLine = data.get("ibm_letter.html");
            assertNotNull(htmlLine);
            body = htmlLine.get("text");
            assertTrue(body.contains("This is a poor facsimilie - it's only really here to register the keywords with Google - but you can go to IBM's original PDF by clicking"));
            
            Map<String, String> xslLine = data.get("tti.xls");
            assertNotNull(xslLine);
            body = xslLine.get("text");
            assertTrue(body.contains("TTI Division Head or Center Director"));
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }
}
