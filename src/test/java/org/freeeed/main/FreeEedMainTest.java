package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.freeeed.services.FreeEedUtil;
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
        System.out.println("FreeEedMainTest.testMain");
        String[] args = new String[2];
        args[0] = "-param_file";
        String platform = PlatformUtil.getPlatform().toString().toLowerCase();
        args[1] = "sample_freeeed_" + platform + ".project";
        // delete output, so that the test should run
        Project project = Project.loadFromFile(new File(args[1]));
        try {
            if (new File(project.getOutputDir()).exists()) {
                Files.deleteRecursively(new File(project.getOutputDir()));
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        FreeEedMain.main(args);
        // TODO - do more tests        
        String outputSuccess = project.getResultsDir() + "/_SUCCESS";
        assertTrue(new File(outputSuccess).exists());
        String metadataFile = project.getResultsDir() + File.separator;
        if (PlatformUtil.getPlatform() == PlatformUtil.PLATFORM.WINDOWS) {
            metadataFile += "metadata.txt";
        } else {
            metadataFile += "part-r-00000";
        }
        assertTrue(new File(metadataFile).exists());
        try {
            int resultCount = FreeEedUtil.countLines(metadataFile);
            System.out.println("FreeEedMainTest.testMain: resultCount = " + resultCount);
            assertTrue("resultCount == 2301", resultCount == 2301);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
