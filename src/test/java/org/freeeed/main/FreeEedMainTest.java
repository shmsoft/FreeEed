package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.freeeed.main.PlatformUtil.PLATFORM;
import org.freeeed.services.Project;
import static org.junit.Assert.assertTrue;
import org.junit.*;

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

    @Test
    public void testMain() {
        System.out.println("main");
        String[] args = new String[2];
        args[0] = "-param_file";
        String platform = PlatformUtil.getPlatform().toString().toLowerCase();
        args[1] = "sample_freeeed_" + platform + ".project";        
        // delete output, so that the test should run
        Project project = Project.getProject();
        try {
            if (new File(project.getResultsDir()).exists()) {
                Files.deleteRecursively(new File(ParameterProcessing.OUTPUT_DIR + File.separator + "output"));
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        FreeEedMain.main(args);
        // TODO - do more tests
        if ((PlatformUtil.getPlatform() == PLATFORM.LINUX) || (PlatformUtil.getPlatform() == PLATFORM.MACOSX)) {
            assertTrue(new File("freeeed_output/output/_SUCCESS").exists());
        }
        String partFile = project.getResultsDir() + File.separator + "part-r-00000";
        try {
            int resultCount = Files.readLines(new File(partFile), Charset.defaultCharset()).size();
            System.out.println("resultCount = " + resultCount);
            assertTrue(resultCount == 2321);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
