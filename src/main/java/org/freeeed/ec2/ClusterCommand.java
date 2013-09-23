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
package org.freeeed.ec2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class ClusterCommand {

    private Cluster cluster;

    public ClusterCommand(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    public boolean runCommandWaitForAll(String cmd) {
        //System.out.println("Cluster cmd = " + cmd);
        ExecutorService es = Executors.newCachedThreadPool();
        for (Server server : cluster) {
            CommandRunner runner = new CommandRunner();
            runner.setServer(server);
            runner.setCmd(cmd);
            server.setCheckerThread(runner);
            es.execute(runner);
        }
        es.shutdown();
        boolean finished = false;
        try {
            finished = es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
        return finished;
    }

    private class CommandRunner extends Thread {

        private Server server;
        private String cmd;

        @Override
        public void run() {
            try {
                //System.out.println("Thread on server " + server.getDnsName() + ", cmd=" + cmd);
                SSHAgent sshAgent = new SSHAgent();
                sshAgent.setHost(server.getDnsName());
                sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
                sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
                sshAgent.executeCommand(cmd);
            } catch (Exception e) {
                // TODO deal with the exception on a per-server basis, 
                // maybe put the exception in the server field somewhere?
                e.printStackTrace(System.out);
            }

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

        /**
         * @return the cmd
         */
        public String getCmd() {
            return cmd;
        }

        /**
         * @param cmd the cmd to set
         */
        public void setCmd(String cmd) {
            this.cmd = cmd;
        }
    }

    public boolean runScpWaitForAll(String from, String to) {
        //System.out.println("runScpWaitForAll from = " + from + " to = " + to);
        ExecutorService es = Executors.newCachedThreadPool();
        for (Server server : cluster) {
            ScpRunner runner = new ScpRunner();
            runner.setServer(server);
            runner.setFromTo(from, to);
            server.setCheckerThread(runner);
            es.execute(runner);
        }
        es.shutdown();
        boolean finished = false;
        try {
            finished = es.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
        return finished;
    }

    private class ScpRunner extends Thread {

        private Server server;
        private String from;
        private String to;

        @Override
        public void run() {
            SSHAgent sshAgent = new SSHAgent();
            sshAgent.setUser(ParameterProcessing.CLUSTER_USER_NAME);
            sshAgent.setKey(ParameterProcessing.PEM_CERTIFICATE_NAME);
            sshAgent.setHost(server.getDnsName());
            try {
//                System.out.println("Scp thread on server " + server.getDnsName()
//                        + ", from = " + from + " to = " + to);
                sshAgent.scpTo(from, to);
            } catch (Exception e) {
                // TODO deal with the exception on a per-server basis, 
                // maybe put the exception in the server field somewhere?
                e.printStackTrace(System.out);
            }

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

        public void setFromTo(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
