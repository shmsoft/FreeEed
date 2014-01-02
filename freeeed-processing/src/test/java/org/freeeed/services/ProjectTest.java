/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.services;

import java.util.List;

import org.freeeed.main.ParameterProcessing;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class ProjectTest {   

    @Test 
    public void testIsPropertyTrue() {
        Project project = Project.setEmptyProject();
        // key absent - result is false
        assertTrue(project.isEmpty());        
        assertFalse(project.isPropertyTrue("mykey"));
        // no boolean value - result is false
        project.put("mykey", "myvalue");        
        assertFalse(project.isPropertyTrue("mykey"));
        // value of "no" results in false
        project.put("mykey-1", "no");
        assertFalse(project.isPropertyTrue("mykey-1"));
        // value of "false" results in false
        project.put("mykey-2", "false");
        assertFalse(project.isPropertyTrue("mykey-2"));
        // value of "true" results in true
        project.put("mykey-3", "true");
        assertTrue(project.isPropertyTrue("mykey-3"));        
    }
    
    @Test
    public void testGetCustodianPatterns() {
        Project project = Project.setEmptyProject();
        
        List<String> custodianPatterns = project.getCustodianPatterns();
        assertNotNull(custodianPatterns);
        assertEquals(1, custodianPatterns.size());
        assertEquals("_(.*?)_",  custodianPatterns.get(0));
        
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "1", "pattern1");
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "2", "pattern2");
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "3", "pattern3");
        
        custodianPatterns = project.getCustodianPatterns();
        assertNotNull(custodianPatterns);
        assertEquals(3, custodianPatterns.size());
        assertEquals("pattern1",  custodianPatterns.get(0));
        assertEquals("pattern2",  custodianPatterns.get(1));
        assertEquals("pattern3",  custodianPatterns.get(2));
    }
    
    @Test
    public void testSetupCurrentCustodianFromFilename() {
        Project project = Project.setEmptyProject();
        
        assertNull(project.getCurrentCustodian());
        
        project.setupCurrentCustodianFromFilename("s3://shmsoft/enron/edrm-enron-v2_slinger-r_pst.zip");
        assertEquals("slinger-r", project.getCurrentCustodian());
        
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "1", ".+Barnard_Pipe/(.+?)_(.*?) ||END||{1} {2}");
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "2", ".+Barnard_Pipe/_(.*?), (.*?)_||END||{2} {1}");
        project.setProperty(ParameterProcessing.CUSTODIAN_PATTERN + "3", ".+Barnard_Pipe/(.+?)_");
        
        project.setupCurrentCustodianFromFilename("s3://shmsoft/weatherford/Barnard_Pipe/Bill_Rouse+(03-01-10+to+03-01-11)+Proofpoint_0.pst");
        assertEquals("Bill Rouse", project.getCurrentCustodian());
        
        project.setupCurrentCustodianFromFilename("s3://shmsoft/weatherford/Barnard_Pipe/McHardGJ_1.pst");
        assertEquals("McHardGJ", project.getCurrentCustodian());
        
        project.setupCurrentCustodianFromFilename("s3://shmsoft/weatherford/Barnard_Pipe/_Berrey%2C+Wayne+H_1.pst");
        assertEquals("Wayne H Berrey", project.getCurrentCustodian());
        
        project.setupCurrentCustodianFromFilename("s3://shmsoft/weatherford/Barnard_Pipe/_McDowell%2C+Fraser_2.pst");
        assertEquals("Fraser McDowell", project.getCurrentCustodian());
    }
}