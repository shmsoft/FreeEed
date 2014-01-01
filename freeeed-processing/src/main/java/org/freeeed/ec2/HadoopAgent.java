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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Util;
import org.freeeed.services.Settings;
import org.freeeed.ui.ClusterControlUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author mark
 */
public class HadoopAgent {
    private final static Logger logger = LoggerFactory.getLogger(HadoopAgent.class);
    private static String mastersFile = "masters";
    private static String slavesFile = "slaves";
    private static String hdfsSiteFile = "hdfs-site.xml";
    private static String coreSiteFile = "core-site.xml";
    private static String mapredSiteFile = "mapred-site.xml";
    private static String settingsFile = "settings.properties";
    private EC2Agent agent;
    private Cluster cluster;
    // if we restart the app, we assume cluster to be ready
    private static boolean hadoopReady = true;

    private ClusterControlUI callingUI;
    
    public HadoopAgent() {
        agent = new EC2Agent();
    }
    
    synchronized public static boolean isHadoopReady() {
        return hadoopReady;
    }

    synchronized public static void setHadoopReady(boolean b) {
        hadoopReady = b;
    }

    public void setupAndStartMC(Cluster cluster) {
        try {
            this.cluster = cluster;
            setupAndStartImpl();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void setupAndStart() {
        try {
            cluster = agent.getRunningInstances(false);
            setupAndStartImpl();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    private void setupAndStartImpl() throws Exception {
        if (cluster.size() == 0) {
            return;
        }
        cluster.assignRoles();
        setupAndStartCluster();
    }

    public void checkHealthMC(Cluster cluster) {
        try {
            this.cluster = cluster;
            checkHealthImpl();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void checkHealth() {
        try {
            cluster = agent.getRunningInstances(true);
            checkHealthImpl();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    private void checkHealthImpl() throws Exception {
        if (cluster.size() == 0) {
            return;
        }
        cluster.assignRoles();
        verifyOperation();
    }

    private void setupAndStartCluster() throws Exception {
        // form config files        
        String masters = cluster.getMaster().getPrivateDnsName() + "\n";
        Files.write(masters.getBytes(), new File(mastersFile));

        List<String> slavesList = new ArrayList<String>();
        for (int i = 0; i < cluster.size(); ++i) {
            Server server = cluster.get(i);
            if (server.isTaskTracker()) {
                slavesList.add(server.getPrivateDnsName());
            }
        }
        String[] slaves = (String[]) slavesList.toArray(new String[0]);
        Files.write(Util.arrayToString(slaves).getBytes(), new File(slavesFile));

        String coreSite = Util.readTextFile("config/" + coreSiteFile);
        coreSite = coreSite.replaceFirst("localhost", cluster.getMaster().getPrivateDnsName());
        Files.write(coreSite.getBytes(), new File(coreSiteFile));

        String mapredSite = Util.readTextFile("config/" + mapredSiteFile);
        mapredSite = mapredSite.replaceFirst("localhost", cluster.getJobTracker().getPrivateDnsName());
        Files.write(mapredSite.getBytes(), new File(mapredSiteFile));

        String cmd;


        String[] output;
        // push config files to the cluster
        logger.info("Configuring the Hadoop cluster");
        ClusterCommand clusterCommand = new ClusterCommand(cluster);
        clusterCommand.runScpWaitForAll(mastersFile, mastersFile);
        clusterCommand.runScpWaitForAll(slavesFile, slavesFile);
        clusterCommand.runScpWaitForAll("config/" + hdfsSiteFile, hdfsSiteFile);
        clusterCommand.runScpWaitForAll(coreSiteFile, coreSiteFile);
        clusterCommand.runScpWaitForAll(mapredSiteFile, mapredSiteFile);
        // copy from home on remote to the config area
        clusterCommand.runCommandWaitForAll("sudo cp " + mastersFile + " /etc/hadoop/conf/");
        clusterCommand.runCommandWaitForAll("sudo cp " + slavesFile + " /etc/hadoop/conf/");
        clusterCommand.runCommandWaitForAll("sudo cp " + hdfsSiteFile + " /etc/hadoop/conf/");
        clusterCommand.runCommandWaitForAll("sudo cp " + coreSiteFile + " /etc/hadoop/conf/");
        clusterCommand.runCommandWaitForAll("sudo cp " + mapredSiteFile + " /etc/hadoop/conf/");
        // create /mnt/tmp for everyone to use
        clusterCommand.runCommandWaitForAll("sudo rm -fr /mnt/tmp");
        clusterCommand.runCommandWaitForAll("sudo mkdir /mnt/tmp");
        clusterCommand.runCommandWaitForAll("sudo chmod 777 /mnt/tmp");
        // create /mnt/tmp for hadoop tmp dir
        clusterCommand.runCommandWaitForAll("sudo mkdir /mnt/tmp/hadoop");
        clusterCommand.runCommandWaitForAll("sudo chmod 777 /mnt/tmp/hadoop");

        logger.info("Hadoop cluster configured, starting the services");
        // shut down all services
        // clean up dfs on slaves
        hadoopReady = false;
        cmd = "for service in /etc/init.d/hadoop-0.20-*; do sudo $service stop; done";
        clusterCommand.runCommandWaitForAll(cmd);
        cmd = "sudo rm -fr /var/lib/hadoop-0.20/cache/*";
        clusterCommand.runCommandWaitForAll(cmd);

        SSHAgent sshAgent = new SSHAgent();
        sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
        sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
        sshAgent.setHost(cluster.getMaster().getDnsName());

        cmd = "sudo -u hdfs hadoop namenode -format";
        sshAgent.executeCommand(cmd);

        cmd = "sudo service hadoop-0.20-namenode start";
        output = sshAgent.executeCommand(cmd);
        logger.info(Util.arrayToString(output));

        // start all hdfs slaves
        clusterCommand = new ClusterCommand(cluster.getDataNodes());
        cmd = "sudo service hadoop-0.20-datanode start";
        clusterCommand.runCommandWaitForAll(cmd);        
        // start all tasktrackers
        clusterCommand = new ClusterCommand(cluster.getTaskTrackers());
        cmd = "sudo service hadoop-0.20-tasktracker start";
        clusterCommand.runCommandWaitForAll(cmd);        
        
        sshAgent.setHost(cluster.getJobTracker().getDnsName());
        cmd = "sudo service hadoop-0.20-jobtracker start";
        output = sshAgent.executeCommand(cmd);
        logger.info(Util.arrayToString(output));
        logger.info("Cluster configuration and startup is complete");

        
        cmd = "sudo rm /usr/lib/hadoop/lib/jets3t*.jar";
        clusterCommand = new ClusterCommand(cluster);
        clusterCommand.runCommandWaitForAll(cmd);
        // install a fresh version of FreeEed
        installFreeEed();
        // run a distributed grep app
        verifyOperation();
        if (callingUI != null) {
            callingUI.refreshStatus();
        }
    }

    private void verifyOperation() throws Exception {
        hadoopReady = false;

        String cmd;
        String[] output;

        SSHAgent sshAgent = new SSHAgent();
        sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
        sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
        sshAgent.setHost(cluster.getJobTracker().getDnsName());
        logger.info("Cluster testing and verification started");
        cmd = "hadoop fs -mkdir /test";
        sshAgent.executeCommand(cmd);

        cmd = "hadoop fs -copyFromLocal *.xml /test/";
        sshAgent.executeCommand(cmd);

        cmd = "hadoop jar /usr/lib/hadoop/hadoop-0.20.2-cdh*-examples.jar grep /test /test-output 'dfs[a-z.]+'";
        output = sshAgent.executeCommand(cmd);
        logger.info(Util.arrayToString(output));

        cmd = "hadoop fs -ls /test-output";
        output = sshAgent.executeCommand(cmd);
        logger.info(Util.arrayToString(output));
        logger.info("Cluster testing and verification is complete");

        boolean success = false;
        for (String line : output) {
            if (line.contains("_SUCCESS")) {
                success = true;
                cluster.setReadyToUse(true);
                break;
            }
        }
        hadoopReady = success;
    }

    private void installFreeEed() throws Exception {
        String url = Settings.getSettings().getDownloadLink();
        logger.info("Installing FreeEed software from " + url);
        String cmd = "rm FreeEed.zip; "
                + "wget " + url + " -O FreeEed.zip --no-check-certificate; "
                + "rm -fr FreeEed; "
                + "unzip -P 4ushH7XZT1 FreeEed.zip";
        SSHAgent sshAgent = new SSHAgent();
        sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
        sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
        sshAgent.setHost(cluster.getJobTracker().getDnsName());
        sshAgent.executeCommand(cmd);
        logger.info("Successfully installed FreeEed");
        // copy the settings to jobtracker
        Server server = cluster.getJobTracker();
        sshAgent.setHost(server.getDnsName());

        Settings cloneForS3 = Settings.getSettings().cloneForS3();
        String settingsFileToUse = "settings.properties.s3";
        Util.writeTextFile(settingsFileToUse, cloneForS3.toString());
        
        logger.info("Copying settings file: {}", settingsFileToUse);

        sshAgent.scpTo(settingsFileToUse, "FreeEed/" + ParameterProcessing.DEFAULT_SETTINGS);
    }

    /**
     * @return the callingUI
     */
    public ClusterControlUI getCallingUI() {
        return callingUI;
    }

    /**
     * @param callingUI the callingUI to set
     */
    public void setCallingUI(ClusterControlUI callingUI) {
        this.callingUI = callingUI;
    }
}
