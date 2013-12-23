/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.services;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
}