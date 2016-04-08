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
package org.freeeed.ec2;

import com.google.common.io.Files;
import com.jcraft.jsch.JSchException;
import com.xerox.amazonws.ec2.EC2Exception;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.util.OsUtil;
import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class ProcessAgent implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAgent.class);
    private boolean upload;
    private boolean process;
    private boolean download;
    private boolean shutdown;
    private SSHAgent sshAgent;
    
    public enum STATE {
        
        UPLOAD, PROCESS, DOWNLOAD, DONE
    };
    private STATE state;
    private int percentComplete;
    
    synchronized public int getPercentComplete() {
        return percentComplete;
    }
    
    synchronized private void setPercentComplete(int percentComplete) {
        this.percentComplete = percentComplete;
    }
    
    synchronized public STATE getState() {
        return state;
    }
    
    synchronized private void setState(STATE state) {
        this.state = state;
    }
    
    @Override
    public void run() {
        try {
            runCompleteProcess();
        } catch (EC2Exception | IOException e) {
            logger.error("Problem detected", e);
        }
    }

    /**
     * @return the upload
     */
    public boolean isUpload() {
        return upload;
    }

    /**
     * @param upload the upload to set
     */
    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    /**
     * @return the process
     */
    public boolean isProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(boolean process) {
        this.process = process;
    }

    /**
     * @return the download
     */
    public boolean isDownload() {
        return download;
    }

    /**
     * @param download the download to set
     */
    public void setDownload(boolean download) {
        this.download = download;
    }

    /**
     * @return the shutdown
     */
    public boolean isShutdown() {
        return shutdown;
    }

    /**
     * @param shutdown the shutdown to set
     */
    public void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
    
    private void runCompleteProcess() throws EC2Exception, IOException {
        if (upload) {
            logger.info("Starting upload");
            runUpload();
        }
        if (process) {
            logger.info("Starting processing");
            try {
                runProcess();
            } catch (Exception e) {
                logger.error("Could not complete the process", e);
            }
        }
        if (download) {
            logger.info("Starting download from S3 to local");
            try {
                runDownload();
            } catch (Exception e) {
                logger.error("Processing error", e);
            }
        }
        if (shutdown) {
            logger.info("Shutting down the cluster, as requested");
            shutdownCluster();
        }
        logger.info("Complete processing done");
        setState(STATE.DONE);
    }
    
    public String getUploadPlan() throws IOException {
        StringBuilder builder = new StringBuilder();
        Project project = Project.getProject();
        List<String> zipFiles = Files.readLines(
                new File(project.getInventoryFileName()),
                Charset.defaultCharset());
        String bucket = Settings.getSettings().getProjectBucket();
        if (bucket == null) {
            return "Bucket not selected";
        }
        builder.append("Upload plan:\n");
        builder.append("S3 bucket: ").append(bucket).append("\n");
        for (String zipFile : zipFiles) {
            //String s3key = zipFile.substring(ParameterProcessing.OUTPUT_DIR.length() + 1);
            String s3key = S3Agent.pathToKey(zipFile);
            builder.append(zipFile).append("=>" + "s3://").append(bucket).append("/").
                    append(s3key).append("\n");
        }
        return builder.toString();
    }
    
    private void runUpload() throws IOException {
        setState(STATE.UPLOAD);
        setPercentComplete(0);
        // first upload the project itself
        S3Agent s3agent = new S3Agent();
        Project project = Project.getProject();
        String projectKey = project.getProjectCode() + ".project";
        s3agent.putProjectInS3(project, projectKey);
        setPercentComplete(1);
        
        List<String> zipFiles = project.getInventory();
        int numberFilesUploaded = 0;
        for (String zipFile : zipFiles) {
            // if that is a local file that exists, upload it, but if not -
            // just assume that it is a URI, and nothing needs to be done
            if (new File(zipFile).exists()) {
                String s3key = S3Agent.pathToKey(zipFile);
                s3agent.putFileInS3(zipFile, s3key);
            }
            ++numberFilesUploaded;
            int pc = 1 + (99 * numberFilesUploaded) / zipFiles.size();
            setPercentComplete(pc);
        }
    }
    
    private void runProcess() throws Exception {
        setState(STATE.PROCESS);
        setPercentComplete(0);
        // now modify the settings for S3 and upload that under a related name
        Project project = Project.getProject();
        Project s3project = project.cloneForS3();
        String s3projectName = project.getProjectCode() + ".project.s3";
        Util.writeTextFile(s3projectName, s3project.toString());
        
        EC2Agent agent = new EC2Agent();
        Cluster cluster = agent.getRunningInstances(false);
        cluster.assignRoles();
        Server server = cluster.getJobTracker();
        
        sshAgent = new SSHAgent();
        sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
        sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
        sshAgent.setHost(server.getDnsName());
        
        sshAgent.scpTo(s3projectName, "FreeEed/" + s3projectName);

        // TODO convert to a running object with status
        Settings settings = Settings.getSettings();
        String cmd = "cd FreeEed; ./run_hadoop_s3.sh "
                + s3projectName
                + " /freeeed/output "
                + settings.getNumReduce();
        logger.info("Running command: {}", cmd);
        String[] results = sshAgent.executeCommand(cmd);
        logger.info("Results: {}", Util.arrayToString(results));
        setPercentComplete(100);
    }
    
    private void runDownload() throws Exception {
        setState(STATE.DOWNLOAD);
        setPercentComplete(0);
        Project project = Project.getProject();
        
        String run = project.getRun();
        if (!run.isEmpty()) {
            run = run + "/";
        }
        String s3key = project.getProjectCode() + "/output/"
                + run
                + "results/";
        
        S3Agent s3agent = new S3Agent();
        String outputDir = project.getOut() + "/" + s3key;
        if (OsUtil.isWindows()) {
            outputDir = outputDir.replaceAll("/", "\\\\");
        }
        // if staging was not done locally, but rather URI data was used,
        // then this directory won't be present - create it        
        Util.deleteDirectory(new File(outputDir));
        new File(outputDir).mkdirs();
        s3agent.getFilesFromS3(s3key, outputDir);
        setPercentComplete(100);
    }
    
    private void shutdownCluster() throws EC2Exception {
        EC2Agent agent = new EC2Agent();
        agent.terminateInstances();
    }
    
    public int getUploadPercent() {
        if (state == STATE.DONE) {
            return 100;
        }
        if (state == STATE.UPLOAD) {
            return percentComplete;
        } else {
            return 100;
        }
    }
    
    public int getProcessPercent() {
        if (state == STATE.DONE) {
            return 100;
        }
        if (state == STATE.UPLOAD) {
            return 0;
        } else if (state == STATE.PROCESS) {
            return percentComplete;
        } else {
            return 100;
        }
    }
    
    public int getDownloadPercent() {
        if (state == STATE.DONE) {
            return 100;
        }
        if (state == STATE.DOWNLOAD) {
            return percentComplete;
        } else {
            return 0;
        }
    }
    
    public boolean isDone() {
        return state == STATE.DONE;
    }
    
    public SSHAgent getSshAgent() {
        return sshAgent;
    }
    
    public void killAllJobs() throws IOException, JSchException {
        EC2Agent agent = new EC2Agent();
        Cluster cluster = agent.getRunningInstances(false);
        cluster.assignRoles();
        
        Server server = cluster.getJobTracker();
        sshAgent = new SSHAgent();
        sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
        sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
        sshAgent.setHost(server.getDnsName());
        String cmd = "hadoop job -list";
        String[] results = sshAgent.executeCommand(cmd);
        String lastLine = results[results.length - 1];
        String jobId = lastLine.split(("\\W"))[0];
        if (jobId != null && !"JobId".equals(jobId)) {
            cmd = "hadoop job -kill " + jobId;
            logger.info("Running command: {}", cmd);
            results = sshAgent.executeCommand(cmd);
            logger.info("Results: {}", Util.arrayToString(results));
        } else {
            logger.info("No jobs running, nothing to stop");
        }
    }
}
