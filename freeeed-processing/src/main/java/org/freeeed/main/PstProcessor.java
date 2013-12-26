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
import java.util.Arrays;
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
        extractEmails(outputDir);
        collectEmails(outputDir);
    }

    /** 
     * Collect all emails in a directory, together with their attachments.
     * Here is a calculation showing why it should work. The largest PST may be 100GB, and the smallest average email
     * size is 10KB. So the max number of emails we can have is 10**7. If each file name is 100 bytes on the averages,
     * we will need 10^9 bytes to store this in a sorted array. That is 1 GB, in the worst possible case.
     * Therefore, it is  safe to sort all files in one directory.
     * 
     * @param emailDir - directory to collect emails from
     * @throws IOException on any problem reading those emails from the directory
     * @throws InterruptedException on any MR problem (throws by Context)
     */
    private void collectEmails(String emailDir) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context, luceneIndex);
            fileProcessor.process();
        } else {
            File files[] = new File(emailDir).listFiles();
            // TODO sort so that 10, 10-djjd should follow
            Arrays.sort(files);
            for (int f = 0; f < files.length; ++f) {
                File file = files[f];
                if (hasAttachments(f, files)) {
                    logger.trace("File {} has attachments", file.getName());
                }
                collectEmails(file.getPath());
            }
        }
    }

    /**
     * Determine if there are attachments following this file in the 
     * @param file
     * @return 
     */
    private boolean hasAttachments(int f, File[] files) {
        return false;
//        return files[f].isFile() && 
//                f < files.length - 1 && 
//                files[f + 1].isFile() && 
//                files[f + 1].getName().contains("-");
    }
    /**
     * Extract the emails with appropriate options.
     *
     */
    public void extractEmails(String outputDir) throws IOException, Exception {
        boolean useJpst = !PlatformUtil.isNix() || Settings.getSettings().isUseJpst();
        if (!useJpst) {
            if (!PlatformUtil.isReadpst()) {
                logger.error("Need to run readpst, but it is not present");
                return;
            }
        }
        new File(outputDir).mkdirs();
        if (useJpst) {
            // TODO implement partial extraction
            String cmd = "java -jar proprietary_drivers/jreadpst.jar "
                    + pstFilePath + " "
                    + outputDir;
            // TODO what if we are in Windows, do we still run Linux command ;) ?
            PlatformUtil.runUnixCommand(cmd);
        } else {
            // start a timer thread to periodically inform Hadoop that we are alive
            // the assumption is that readpst is very stable
            Timer timer = new Timer(refreshInterval, this);
            timer.start();
            String command = "readpst -e -D -b -S -o " + outputDir + " " + pstFilePath;
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
