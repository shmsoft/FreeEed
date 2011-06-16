package org.freeeed.main;

import java.util.List;
import java.util.StringTokenizer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
        assert (true);

        System.out.println("dry run");
        String[] args1 = {"-param_file", "my.freeeed.properties", "-dry"};
        FreeEedMain.main(args1);
        // TODO - dump results, verify that parameters file was created
        assert (true);


        System.out.println("complete staging and processing");
        String[] args2 = {"-param_file", "my.freeeed.properties", "-stage", "-process", "local"};
        FreeEedMain.main(args2);
        // TODO - verify that results were created
        String command = "wc test-output/output/part-r-00000";
        List<String> output = LinuxUtil.runLinuxCommand(command);
        
        assert (output.size() > 0);
        StringTokenizer tokenizer = new StringTokenizer(output.get(0));
        int numberDocuments = Integer.parseInt(tokenizer.nextToken()) - 2;
        System.out.println("numberDocuments = " + numberDocuments);
        assert(numberDocuments > 0);       
    }
}
