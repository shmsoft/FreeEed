/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.main;

import org.apache.commons.configuration.Configuration;
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
public class FreeEedMainTest {
    
    public FreeEedMainTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class FreeEedMain.
     */
    @Test
    public void testMain() {
        String[] args0 = {"-help"};
        FreeEedMain.main(args0);
        // TODO - just tell them to look for help
        assert(true);        
        
        System.out.println("dry run");
        String[] args1 = {"-param_file", "my.freeeed.properties", "-dry"};
        FreeEedMain.main(args1);
        // TODO - dump results, verify that parameters file was created
        assert(true);        

        System.out.println("package input");
        String[] args2 = {"-input", "test-data/01-one-time-test"};
        FreeEedMain.main(args2);
        // TODO - verify that input was zipped
        assert(true);

        System.out.println("process");
        String[] args3 = {"-process", "local"};
        FreeEedMain.main(args3);
        // TODO - verify that results were created
        assert(true);        
    }
}
