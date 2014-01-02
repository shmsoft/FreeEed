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
import java.util.Comparator;
import javax.swing.Timer;
import org.apache.hadoop.io.MD5Hash;
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
        collectEmails(outputDir, false, null);
    }

    /**
     * Collect all emails in a directory, together with their attachments. Here is a calculation showing why it should
     * work. The largest PST may be 100GB, and the smallest average email size is 10KB. So the max number of emails we
     * can have is 10**7. If each file name is 100 bytes on the averages, we will need 10^9 bytes to store this in a
     * sorted array. That is 1 GB, in the worst possible case. Therefore, it is safe to sort all files in one directory.
     *
     * @param emailDir - directory to collect emails from
     * @throws IOException on any problem reading those emails from the directory
     * @throws InterruptedException on any MR problem (throws by Context)
     */
    private void collectEmails(String emailDir, boolean hasAttachments, MD5Hash hash) throws IOException, InterruptedException {
        if (new File(emailDir).isFile()) {
            EmlFileProcessor fileProcessor = new EmlFileProcessor(emailDir, context, luceneIndex);
            fileProcessor.process(hasAttachments, hash);
        } else {
            File files[] = new File(emailDir).listFiles();
            Arrays.sort(files, new MailWithAttachmentsComparator());
            for (File file : files) {
                logger.trace(file.getPath());
            }
            for (int f = 0; f < files.length; ++f) {
                int attachmentCount = getAttachmentCount(f, files);
                if (attachmentCount == 0) {
                    collectEmails(files[f].getPath(), false, null);
                } else {
                    logger.debug("File {} has {} attachments", files[f].getName(), attachmentCount);
                    MD5Hash parentHash = Util.createKeyHash(files[f], null);
                    collectEmails(files[f].getPath(), true, null);
                    for (int a = 1; a <= attachmentCount; ++a) {
                        collectEmails(files[f + a].getPath(), false, parentHash);
                    }
                    f += attachmentCount;
                }
            }
        }
    }

    /**
     * Determine if the file in the array has attachments following it in order, and tell us the count.
     *
     * @param f file position in the array.
     * @param files array of file to analyze.
     * @return 0 if there are no attachments, positive integer if there are some.
     */
    private int getAttachmentCount(int f, File[] files) {
        int attachmentCount = 0;
        for (int n = 1; n < files.length - 1 - f; ++n) {
            if (files[f].isFile()
                    && f + n < files.length
                    && files[f + n].isFile()
                    && files[f].getName().matches("\\d+")
                    && files[f + n].getName().startsWith(files[f].getName() + "-")) {
                attachmentCount = n;
            } else {
                break;
            }
        }
        return attachmentCount;
    }

    /**
     * Determine if the given file is an attachment to the given parent. This is determined by the parent having a name
     * like "23 and attachment - like "23-excel.xls".
     *
     * @param file file whose name is to be analyzed for being a child.
     * @param parent parent file name to be analyzed for parenthood.
     * @return true if file is an attachment of parent.
     */
    private boolean isAttachment(File file, File parentFile) {
        return parentFile != null && file.getName().startsWith(parentFile.getName() + "-");
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
            logger.info("Will use readpst...");
            // start a timer thread to periodically inform Hadoop that we are alive
            // the assumption is that readpst is very stable
            Timer timer = new Timer(refreshInterval, this);
            timer.start();
            String command = "readpst -e -D -b -S -o " + outputDir + " " + pstFilePath;
            PlatformUtil.runUnixCommand(command);

            logger.info("readpst finished!");
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

    /**
     * Sort according to the file naming created by readpst.
     */
    class MailWithAttachmentsComparator implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
            if (file1.isDirectory() || file2.isDirectory()) {
                return file1.getPath().compareTo(file2.getPath());
            }
            int comparePath = file1.getParent().compareTo(file2.getParent());
            if (comparePath != 0) {
                return comparePath;
            }
            String fileName1 = file1.getName();
            String fileName2 = file2.getName();
            int nfile1, nfile2;
            try {
                nfile1 = Integer.parseInt(fileName1);
                nfile2 = Integer.parseInt(fileName2);
                return new Integer(nfile1).compareTo(new Integer(nfile2));
            } catch (NumberFormatException e) {
                // fall through and process attachments
            }
            try {
                int index1 = fileName1.indexOf('-');
                if (index1 > 0) {
                    nfile1 = Integer.parseInt(fileName1.substring(0, index1));
                } else {
                    nfile1 = Integer.parseInt(fileName1);
                }
                int index2 = fileName2.indexOf('-');
                if (index2 > 0) {
                    nfile2 = Integer.parseInt(fileName2.substring(0, index2));
                } else {
                    nfile2 = Integer.parseInt(fileName2);
                }
                if (nfile1 < nfile2) {
                    return -1;
                }
                if (nfile1 > nfile2) {
                    return 1;
                }
                if (index1 < 0) {
                    return -1;
                }
                if (index2 < 0) {
                    return 1;
                }
                return fileName1.compareTo(fileName2);
            } catch (NumberFormatException e) {
                logger.warn("Unexpected problem parsing email file name", e);
                return fileName1.compareTo(fileName2);
            }
        }
    }
}
