/*
 *
 * Copyright SHMsoft, Inc. 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.Timer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.services.Util;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PstProcessor implements ActionListener {

    private String pstFilePath;
    private Context context;
    private static int refreshInterval = 60000;
    private LuceneIndex luceneIndex;
    private static Logger logger = LoggerFactory.getLogger(PstProcessor.class);

    /**
     *
     * @param pstFilePath
     * @param context
     * @param luceneIndex
     */
    public PstProcessor(String pstFilePath, Context context, LuceneIndex luceneIndex) {
        // TODO - must we have such strange parameters? Is there a better structure?
        this.pstFilePath = pstFilePath;
        this.context = context;
        this.luceneIndex = luceneIndex;
        logger.debug("PST extraction with pstFilePath = {}", pstFilePath);
    }

    /**
     * Determine whether a given file is a Microsoft Outlook file. There are two tests here: firstly, the file must have
     * the extension of *.pst. That is because PST files are under our control, and we do not expect that they will come
     * in without *.pst extension. Other MS Outlook extension may be added later on. Secondly, we do the Unix 'file'
     * command, to confirm this test. We will not attempt the extraction if Unix does not recognize it. That's for *nix.
     * For Windows, we do the JPST tricks.
     *
     * @param fileName file path to be analyzed.
     * @return yes if file is a MS Outlook file, false if it is not.
     */
    public static boolean isPST(String fileName) {
        logger.trace("Determine isPST for file {}", fileName);
        boolean isPst = false;
        if ("pst".equalsIgnoreCase(Util.getExtension(fileName))) {            
            if (PlatformUtil.isNix()) {
                String fileType = PlatformUtil.getFileType(fileName);
                logger.trace("In *nix, file type is {}", fileType);
                isPst = fileType.startsWith("Microsoft Outlook");
            } else if (PlatformUtil.isWindows()) {
                // TODO use JPST to verify PST type
                isPst = true;
            }
        }
        logger.trace("isPst results: {}", isPst);
        return isPst;
    }

    public void process() throws IOException, Exception {
        String outputDir = Settings.getSettings().getPSTDir();
        File pstDirFile = new File(outputDir);
        if (pstDirFile.exists()) {
            Util.deleteDirectory(pstDirFile);
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
     * Extract the emails with appropriate options, follow this sample format readpst -e -D -o myoutput
     * zl_bailey-s_000.pst
     *
     */
    // TODO why do we pass pstPath when the processor already has it as a member?
    public void extractEmails(String pstPath, String outputDir) throws IOException, Exception {
        boolean useJpst = !PlatformUtil.isNix() || Settings.getSettings().isUseJpst();
        if (!useJpst) {
            String error = PlatformUtil.verifyReadpst();
            if (!error.isEmpty()) {
                logger.error("Need to run readpst, but it is not present");
                return;
            }
        }
        new File(outputDir).mkdirs();
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
            String command = "readpst -e -D -o " + outputDir + " " + pstPath;
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
