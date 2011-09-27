package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import junit.framework.TestCase;
import org.freeeed.main.PlatformUtil.PLATFORM;

public class FreeEedMainTest extends TestCase {

    public FreeEedMainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of main method, of class FreeEedMain.
     */
    public void testMain() {
        System.out.println("main");
        String[] args = new String[2];
        args[0] = "-param_file";
        args[1] = "sample_freeeed.project";
        // delete output, so that the test should run
        try {
            if (new File(ParameterProcessing.OUTPUT_DIR + "/output").exists()) {
                Files.deleteRecursively(new File(ParameterProcessing.OUTPUT_DIR + "/output"));
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        FreeEedMain.main(args);
        // TODO - do more tests
        assert (!(PlatformUtil.getPlatform() == PLATFORM.LINUX)
                || new File("freeeed_output/output/_SUCCESS").exists());
    }
}
