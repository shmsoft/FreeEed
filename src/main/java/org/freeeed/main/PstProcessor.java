package org.freeeed.main;

import org.freeeed.services.FreeEedUtil;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;

public class PstProcessor {

    private String pstFilePath;
    private Context context;

    public PstProcessor(String pstFilePath, Context context) {
        this.pstFilePath = pstFilePath;
        this.context = context;
    }
    // TODO improve PST file type detection

    public static boolean isPST(String fileName) {
        if ("pst".equalsIgnoreCase(FreeEedUtil.getExtension(fileName))) {
            return true;
        }
        return false;
    }

    public void process() throws IOException, Exception {
        String outputDir = ParameterProcessing.PST_OUTPUT_DIR;
        File pstDirFile = new File(outputDir);
        if (pstDirFile.exists()) {
            Files.deleteRecursively(pstDirFile);
        }
        extractEmails(pstFilePath, outputDir);
        collectEmails(outputDir);
    }

    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context);
            fileProcessor.process();
        } else {
            File files[] = new File(emailDir).listFiles();
            for (File file : files) {
                collectEmails(file.getPath());
            }
        }
    }

    /**
     * Extract the emails with appropriate options, follow this sample format
     * readpst -M -D -o myoutput zl_bailey-s_000.pst
     */
    public static void extractEmails(String pstPath, String outputDir) throws IOException, Exception {
        Project project = Project.getProject();
        boolean useJpst = (PlatformUtil.getPlatform() != PlatformUtil.PLATFORM.LINUX && 
                PlatformUtil.getPlatform() != PlatformUtil.PLATFORM.MACOSX)
                || Settings.getSettings().isUseJpst();
        if (!useJpst) {
            String error = PlatformUtil.verifyReadpst();
            if (error != null) {
                System.out.println("Warning: running readpst, but it is not present");
                return;
            }
        }
        new File(outputDir).mkdir();
        // if we are not in Linux, or if readpst is not present, or if the flag tells us so -
        // then use the JPST
        if (useJpst) {
            String cmd = "java -jar proprietary_drivers/jreadpst.jar "
                    + pstPath + " "
                    + outputDir;
            PlatformUtil.runUnixCommand(cmd);
        } else {
            String command = "readpst -M -D -o " + outputDir + " " + pstPath;
            PlatformUtil.runUnixCommand(command);
        }
    }
}
