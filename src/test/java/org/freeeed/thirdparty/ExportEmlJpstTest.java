package org.freeeed.thirdparty;

import com.google.common.io.Files;
import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;

import org.freeeed.main.FreeEedMain;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.PlatformUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mark
 */
public class ExportEmlJpstTest {

    private String pstPath = "test-data/03-enron-pst/zl_bailey-s_000.pst";

    public ExportEmlJpstTest() {
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
     * Test of extractEml method, of class ExportEmlJpst.
     */
    @Test
    // at the moment, no test is run here
    public void testExtractEml() {
        Configuration configuration = FreeEedMain.getInstance().getProcessingParameters();
        boolean useJpst = PlatformUtil.getPlatform() != PlatformUtil.PLATFORM.LINUX
                || configuration.containsKey(ParameterProcessing.USE_JPST);
        if (!useJpst) {
            return;
        }
        try {
            System.out.println("extractEml");
            String pstOutputDir = "pst_output";
            if (new File(pstOutputDir).exists()) {
                Files.deleteRecursively(new File(pstOutputDir));
            }
            // ths would be direct test
//            ExportEmlJpst instance = new ExportEmlJpst();
//            instance.extractEml(pstPath, pstOutputDir);
            // this would be outside process test
            String cmd = "java "
                    + "-cp target/FreeEed-1.0-SNAPSHOT-jar-with-dependencies.jar "
                    + "org.freeeed.thirdparty.ExportEmlJpst "
                    + pstPath + " "
                    + pstOutputDir;
            PlatformUtil.runLinuxCommand(cmd);
            int countEmails = FileUtils.listFiles(
                    new File(pstOutputDir),
                    null, true).size();
            assert (countEmails == 2178);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assert (false);
        }
    }
}
