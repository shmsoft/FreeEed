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
package org.freeeed.mr;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.freeeed.data.index.LuceneIndex;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.ec2.S3Agent;
import org.freeeed.mail.EmailProperties;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.PstProcessor;
import org.freeeed.main.ZipFileProcessor;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Stats;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.Files;
import org.freeeed.services.DuplicatesTracker;
import org.freeeed.services.UniqueIdGenerator;
import org.freeeed.ui.ProcessProgressUI;

/**
 * Maps input key/value pairs to a set of intermediate key/value pairs.
 *
 * @author mark
 */
public class FreeEedMapper
//        extends Mapper<LongWritable, Text, Text, MapWritable>
{

    private final static Logger LOGGER = LoggerFactory.getLogger(FreeEedMapper.class);
    private String[] inputs;
    private String zipFile;
    private String custodian;
    private LuceneIndex luceneIndex;
    private MetadataWriter metadataWriter;

    /**
     * Called once for each key/value pair in the input split.
     *
     * @param key Key of input.
     * @param value Value of input.
     * @param context Holds result key/value after process, as well as other
     * parameters.
     * @throws IOException if any IO errors occurs.
     * @throws InterruptedException if thread is interrupted.
     */
//    @Override
//    public void map(LongWritable key, Text value, Mapper.Context context)
//            throws IOException, InterruptedException {
//        inputs = value.toString().split(";");
//        processZipFile(inputs);
//    }

    /**
     * Process all individual files in the staging entry
     */
    private void processZipFile(String[] inputs)
            throws IOException, InterruptedException {
        Project project = Project.getCurrentProject();
        zipFile = inputs[0].split(",")[0];
        custodian = inputs[0].split(",")[1];
        // no empty or incorrect lines!
        if (zipFile.trim().isEmpty()) {
            return;
        }
        LOGGER.info("Processing: {}", zipFile);
        if (inputs.length >= 3) {
            project.setMapItemStart(Integer.parseInt(inputs[1]));
            project.setMapItemEnd(Integer.parseInt(inputs[2]));
            LOGGER.info("From {} to {}", project.getMapItemStart(), project.getMapItemEnd());
        }
        Stats.getInstance().setZipFileName(zipFile);
        updateProgressUI(zipFile);

        project.setCurrentCustodian(custodian);
        // metadataWriter is initialized below
        // assuming that one mapper gets only one input file
        // if this architecture ever changes, the code below will change too
        if (metadataWriter == null) {
            metadataWriter = new MetadataWriter();
            try {
                metadataWriter.setup();
            } catch (IOException e) {
                LOGGER.error("metadataWriter error", e);
            }

        }
        LOGGER.info("Will use current custodian: {}", project.getCurrentCustodian());
        // if we are in Hadoop, copy to local tmp         
        if (project.isEnvHadoop()) {
            String extension = org.freeeed.services.Util.getExtension(zipFile);
            String tmpDir = ParameterProcessing.TMP_DIR_HADOOP;
            File tempZip = File.createTempFile("freeeed", ("." + extension), new File(tmpDir));
            tempZip.delete();
            if (project.isFsHdfs() || project.isFsLocal()) {
                String cmd = "hadoop fs -copyToLocal " + zipFile + " " + tempZip.getPath();
                OsUtil.runCommand(cmd);
            } else if (project.isFsS3()) {
                S3Agent s3agent = new S3Agent();
                s3agent.getStagedFileFromS3(zipFile, tempZip.getPath());
            }
            zipFile = tempZip.getPath();
        }
        if (PstProcessor.isPST(zipFile)) {
            try {
                new PstProcessor(zipFile, metadataWriter, luceneIndex).process();
            } catch (Exception e) {
                LOGGER.error("Problem with PST processing...", e);
            }
        } else {
            LOGGER.info("Will create Zip File processor for: {}", zipFile);
            // process archive file
            ZipFileProcessor processor = new ZipFileProcessor(zipFile, metadataWriter, luceneIndex);
            processor.process(false, null);
        }
    }

    private void updateProgressUI(String zipFileName) {
        ProcessProgressUI ui = ProcessProgressUI.getInstance();
        if (ui != null) {
            ui.setProcessingState(new File(zipFileName).getName());
            ui.setTotalSize(1000);
            ui.updateProgress(0);
        }
    }

//    @Override
//    protected void setup(Mapper.Context context) {
//        String settingsStr = context.getConfiguration().get(ParameterProcessing.SETTINGS_STR);
//        Settings settings = Settings.loadFromString(settingsStr);
//        Settings.setSettings(settings);
//
//        String projectStr = context.getConfiguration().get(ParameterProcessing.PROJECT);
//        Project project = Project.loadFromString(projectStr);
//        if (project.isEnvHadoop()) {
//            // we need the system check only if we are not in local mode
//            OsUtil.systemCheck();
//            List<String> status = OsUtil.getSystemSummary();
//            for (String stat : status) {
//                LOGGER.info(stat);
//            }
//            Configuration conf = context.getConfiguration();
//            String taskId = conf.get("mapred.task.id");
//            if (taskId != null) {
//                Settings.getSettings().setProperty("mapred.task.id", taskId);
//            }
//
//            String metadataFileContents = context.getConfiguration().get(EmailProperties.PROPERTIES_FILE);
//            try {
//                new File(EmailProperties.PROPERTIES_FILE).getParentFile().mkdirs();
//                Files.write(metadataFileContents.getBytes(), new File(EmailProperties.PROPERTIES_FILE));
//            } catch (IOException e) {
//                LOGGER.error("Problem writing the email properties file to disk", e);
//            }
//        }
//        // initializations section
//        UniqueIdGenerator.getInstance().reset();
//        DuplicatesTracker.getInstance().reset();
//        Stats.getInstance().incrementMapperCount();
//    }

//    @Override
//    @SuppressWarnings("unchecked")
//    protected void cleanup(Mapper.Context context) {
//        Project project = Project.getCurrentProject();
//        if (project.getDataSource() == Project.DATA_SOURCE_LOAD_FILE) {
//            return;
//        }
//        Stats stats = Stats.getInstance();
//
//        SolrIndex.getInstance().flushBatchData();
//
//        LOGGER.info("In zip file {} processed {} items", stats.getZipFileName(),
//                stats.getItemCount());
//
//        if (luceneIndex != null) {
//
//            try {
//                luceneIndex.destroy();
//                String zipFileName = luceneIndex.createIndexZipFile();
//
//                String hdfsZipFileName = "/"
//                        + Settings.getSettings().getLuceneIndexDir() + File.separator
//                        + Project.getCurrentProject().getProjectCode() + File.separator
//                        + context.getTaskAttemptID() + ".zip";
//
//                String removeOldZip = "hadoop fs -rm " + hdfsZipFileName;
//                OsUtil.runCommand(removeOldZip);
//
//                String cmd = "hadoop fs -copyFromLocal " + zipFileName + " " + hdfsZipFileName;
//                OsUtil.runCommand(cmd);
//            } catch (IOException e) {
//                LOGGER.error("Error generating lucene index data", e);
//            }
//        }
//        try {
//            metadataWriter.cleanup();
//        } catch (IOException e) {
//            LOGGER.error("Error on mapper cleanup", e);
//        }
//        metadataWriter = null;
//    }
}
