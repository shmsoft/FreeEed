package org.freeeed.main;

import java.io.IOException;

import static org.junit.Assert.*;

import org.freeeed.services.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmlFileProcessorTest {
    
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
        
        EmlFileProcessor emlProcessor = new EmlFileProcessor("test-data/02-loose-files/docs/eml/1.eml", null, null);
        try {
            emlProcessor.process();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
}
