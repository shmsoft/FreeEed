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


import com.xerox.amazonws.ec2.ConsoleOutput;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.ReservationDescription.Instance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.History;
import org.freeeed.services.Settings;


/**
 *
 * @author mark
 */
public class EC2Agent {

    private static Log log = LogFactory.getLog(EC2Agent.class);
    private Jec2 jec2;
    private String availabilityZone;
    private int clusterSize = 1;
    private String keyName;
    private String securityGroup;
    // cached result
    private Cluster cluster;

    public EC2Agent() {
        Settings settings = Settings.getSettings();
        clusterSize = settings.getClusterSize();
        keyName = settings.getKeyPair();
        securityGroup = settings.getSecurityGroup();
        availabilityZone = settings.getAvailabilityZone();
        connect();
    }

    public void launchInstances() throws EC2Exception {
        Settings settings = Settings.getSettings();
        String amiName = settings.getClusterAmi();
        LaunchConfiguration launchConfiguration = new LaunchConfiguration(amiName);
        launchConfiguration.setKeyName(keyName);
        launchConfiguration.setSecurityGroup(Collections.singletonList(getSecurityGroup()));
        launchConfiguration.setAvailabilityZone(availabilityZone);
        launchConfiguration.setMinCount(clusterSize);
        launchConfiguration.setMaxCount(clusterSize);
        String instanceTypeStr = settings.getInstanceType();
        InstanceType instanceType = InstanceType.getTypeFromString(instanceTypeStr);
        launchConfiguration.setInstanceType(instanceType);

        ReservationDescription reservationDescription = jec2.runInstances(launchConfiguration);
    }

    private void connect() {
        Settings settings = Settings.getSettings();
        final String AWSAccessKeyId = settings.getAccessKeyId();
        final String SecretAccessKey = settings.getSecretAccessKey();

        jec2 = new Jec2(AWSAccessKeyId, SecretAccessKey);
        //jec2 = new Jec2(AWSAccessKeyId, SecretAccessKey, true, "ec2.eu-west-1.amazonaws.com");

    }

    public String describeRunningInstances() throws EC2Exception {
        StringBuilder result = new StringBuilder();
        cluster = getRunningInstances(true);
        result.append("Instances:\n");

        for (Server server : cluster) {
            result.append(server.getInstanceId()).append(" ").
                    append(server.getState()).
                    append(server.getInitialized()).append("\n");
        }

        return result.toString();
    }

    public Cluster getRunningInstances(boolean verify) {
        try {
            cluster = new Cluster();
            List<String> params = new ArrayList<String>();
            List<ReservationDescription> instances = jec2.describeInstances(params);
            for (ReservationDescription res : instances) {
                if (res.getInstances() != null) {
                    for (Instance inst : res.getInstances()) {
                        if (!Server.EC2_STOPPED.equalsIgnoreCase(inst.getState())
                                && !Server.EC2_TERMINATED.equalsIgnoreCase(inst.getState())
                                && inst.getImageId().equals(Settings.getSettings().getClusterAmi())) {

                            Server server = new Server();
                            server.setInstanceId(inst.getInstanceId());
                            server.setState(inst.getState());
                            server.setDnsName(inst.getDnsName());
                            server.setPrivateDnsName(inst.getPrivateDnsName());
                            cluster.add(server);
                        }
                    }
                }
            }
            if (verify) {
                setInitializedState(cluster);
            }

        } catch (EC2Exception e) {
            e.printStackTrace(System.out);
            History.appendToHistory(e.getMessage());
        }
        History.appendToHistory("Running instances: " + cluster.size());
        if (verify) {
            History.appendToHistory("Completely initialized: " + cluster.getInitializedCount());
        }
        return cluster;
    }

    private void setInitializedState(Cluster cluster) {
        ExecutorService es = Executors.newCachedThreadPool();
        for (Server server : cluster) {
            LoginChecker checker = new LoginChecker();
            checker.setServer(server);
            server.setCheckerThread(checker);
            es.execute(checker);

        }
        es.shutdown();
        boolean finished = false;
        try {
            finished = es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
        // TODO what to do if 'finished" is false       
    }

    private class LoginChecker extends Thread {

        private Server server;

        @Override
        public void run() {
            try {
                //System.out.println("LoginChecker.run for server " + server);
                SSHAgent sshAgent = new SSHAgent();
                sshAgent.setHost(server.getDnsName());
                sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
                sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
                sshAgent.executeCommand("ls");
            } catch (Exception e) {
                server.setInitialized(false);
                //System.out.println("LoginChecker.run for check " + false);
                return;
            }
            server.setInitialized(true);
            //System.out.println("LoginChecker.run for check " + true);
        }

        /**
         * @return the server
         */
        public Server getServer() {
            return server;
        }

        /**
         * @param server the server to set
         */
        public void setServer(Server server) {
            this.server = server;
        }
    }

    public void getConsoleOutput(String instanceId) throws EC2Exception {
        ConsoleOutput consOutput = jec2.getConsoleOutput(instanceId);
        log.info("Console Output:");
        log.info(consOutput.getOutput());

    }

    public void getKeypairs() throws EC2Exception {
        List<KeyPairInfo> info = jec2.describeKeyPairs(new String[]{});
        log.info("keypair list");
        for (KeyPairInfo i : info) {
            log.info("keypair : " + i.getKeyName() + ", " + i.getKeyFingerprint());
        }
    }

    /**
     * @return the availabilityZone
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    /**
     * @param availabilityZone the availabilityZone to set
     */
    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * @return the clusterSize
     */
    public int getClusterSize() {
        return clusterSize;
    }

    /**
     * @param clusterSize the clusterSize to set
     */
    public void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    /**
     * @return the keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * @param keyName the keyName to set
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public void terminateInstances() throws EC2Exception {
        cluster = getRunningInstances(false);
        List<String> instanceIds = cluster.getInstanceIds();
        if (instanceIds.size() > 0) {
            jec2.terminateInstances(instanceIds);
        }
    }

    /**
     * @return the securityGroup
     */
    public String getSecurityGroup() {
        return securityGroup;
    }

    /**
     * @param securityGroup the securityGroup to set
     */
    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }

    public String getClusterState() {
        String state = "Cluster state: \n";
        // note: using cached cluster to save time
        if (cluster.getRunningCount() < clusterSize
                || cluster.getInitializedCount() < clusterSize) {
            state += "Waiting for all " + clusterSize + " instances to come up";
            return state;
        }
        if (HadoopAgent.isHadoopReady()) {
            state += "Hadoop cluster is set up and ready";
        } else {
            state += "Setting up Hadoop cluster";
        }
        return state;
    }
}
