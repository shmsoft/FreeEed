package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import org.freeeed.main.PlatformUtil.PLATFORM;

/**
 *
 * @author Mark
 */
public class PlatformUtilTest {

    private String pstPath = "test-data/03-enron-pst/zl_bailey-s_000.pst";

    public PlatformUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetPlatform() {
        System.out.println("getPlatform");
        PLATFORM platform = PlatformUtil.getPlatform();
        System.out.println("Platform = " + platform);
    }

    @Test
    public void testReadPst() {
        try {
            System.out.println("getReadPst");
            if (new File(ParameterProcessing.PST_OUTPUT_DIR).exists()) {
                Files.deleteRecursively(new File(ParameterProcessing.PST_OUTPUT_DIR));
            }
            PstProcessor.extractEmails(pstPath, ParameterProcessing.PST_OUTPUT_DIR);
            int countEmails = FileUtils.listFiles(
                    new File(ParameterProcessing.PST_OUTPUT_DIR),
                    null, true).size();
            System.out.println("countEmails = " + countEmails);
            assert (countEmails == 2346);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            assert(false);
        }
    }
}
