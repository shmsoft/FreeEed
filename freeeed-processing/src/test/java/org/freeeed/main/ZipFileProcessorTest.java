package org.freeeed.main;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.freeeed.services.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class ZipFileProcessorTest {

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
    public void testProcess() {
        Project.getProject().setEnvironment(Project.ENV_LOCAL);
        Project.getProject().setProperty(ParameterProcessing.RUN, "123");
        Project.getProject().setProperty(ParameterProcessing.PROJECT_CODE, "test");
        Project.getProject().setCurrentCustodian("ivan");
        Project.getProject().setTextInMetadata(true);
        
        ZipFileProcessor zipProcessor = new ZipFileProcessor("test-data/zip/data.zip", null, null);
        try {
            FileUtils.deleteDirectory(new File("freeeed-output/test/output/123"));
            
            zipProcessor.process();
            
            String metadataContent = Files.toString(new File("freeeed-output/test/output/123/results/metadata.txt"), Charset.forName("UTF-8"));
            System.out.println(metadataContent);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }
}
