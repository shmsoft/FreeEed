package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.Project;
import static org.junit.Assert.assertTrue;
import org.junit.*;

public class FreeEedSmallTest {

    public FreeEedSmallTest() {
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
        System.out.println("testMain");
        String[] args = new String[2];
        args[0] = "-param_file";
        args[1] = "small_test.project";        
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
            //int resultCount = Files.readLines(new File(metadataFile), Charset.defaultCharset()).size();
            int resultCount = FreeEedUtil.countLines(metadataFile);            
            System.out.println("resultCount = " + resultCount);
            assertTrue("resultCount == 9", resultCount == 9);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
