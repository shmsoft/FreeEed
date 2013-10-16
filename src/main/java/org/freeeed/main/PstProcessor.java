/*    
    *
    * Licensed under the Apache License, Version 2.0 (the "License");
    * you may not use this file except in compliance with the License.
    * You may obtain a copy of the License at
    *
    * http://www.apache.org/licenses/LICENSE-2.0
    *
    * Unless required by applicable law or agreed to in writing, software
    * distributed under the License is distributed on an "AS IS" BASIS,
    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    * See the License for the specific language governing permissions and
    * limitations under the License.
*/
package org.freeeed.main;

import com.google.common.io.Files;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.Settings;


public class PstProcessor implements ActionListener {

    private String pstFilePath;
    private Context context;
    private static int refreshInterval = 60000;
    private LuceneIndex luceneIndex;

    public PstProcessor(String pstFilePath, Context context, LuceneIndex luceneIndex) {
        this.pstFilePath = pstFilePath;
        this.context = context;
        this.luceneIndex = luceneIndex;
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
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context, luceneIndex);
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
    public void extractEmails(String pstPath, String outputDir) throws IOException, Exception {
        boolean useJpst = (PlatformUtil.getPlatform() != PlatformUtil.PLATFORM.LINUX
                && PlatformUtil.getPlatform() != PlatformUtil.PLATFORM.MACOSX)
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
            // TODO implement partial extraction
            String cmd = "java -jar proprietary_drivers/jreadpst.jar "
                    + pstPath + " "
                    + outputDir;
            PlatformUtil.runUnixCommand(cmd);
        } else {
            // start a timer thread to periodically inform Hadoop that we are alive
            // the assumption is that readpst is very stable
            Timer timer = new Timer(refreshInterval, this);
            timer.start();
            String command = "readpst -M -D -o " + outputDir + " " + pstPath;
            PlatformUtil.runUnixCommand(command);
            timer.stop();
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // inform Hadoop that we are alive
        if (context != null) {
            context.progress();
        }
    }
}
