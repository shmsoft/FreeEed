package ai.scaia.advisor;

import org.freeeed.mr.FreeEedMR;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This is a mock imitation of the ScaiaAdvisor class
 */
public class ScaiaTextAdvisor extends ScaiaAdvisor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedMR.class);

    public String similarDocs(String corpusDir, String targetFilePath) {
        String aiPath = "/home/mark/projects/SHMsoft/docsimilarity";
        String cmd = "python " +
                aiPath +
                "/docsimilarity.py corpusDir  targetFilePath";
        try {
            OsUtil.runCommand(cmd);
        } catch (IOException ex) {
            LOGGER.error("Running AI", ex);
        }
        return "";
    }
}
