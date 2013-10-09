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

import java.util.ArrayList;
import java.util.List;

import org.freeeed.services.History;

/**
 * Encapsulate information on a Hadoop cluster of EC2 servers.
 * @author mark
 */
public class Cluster extends ArrayList<Server> {
    private boolean readyToUse;
    
    /**
     * Get the instance id's for all the servers.
     * @return list of instance id's.
     */
    public List<String> getInstanceIds() {
        ArrayList<String> instanceIds = new ArrayList<String>();
        for (Server server : this) {
            instanceIds.add(server.getInstanceId());
        }
        return instanceIds;
    }
    
    /**
     * Detect the master as indicated by its designation as NameNode.
     * @return Server which is the master.
     */
    public Server getMaster() {
        for (Server server : this) {
            if (server.isNameNode()) {
                return server;
            }
        }
        return null;
    }
    /**
     * Detect the job tracker as indicated by its designation as NameNode.
     * @return Server which is the master.
     */
    public Server getJobTracker() {
        for (Server server : this) {
            if (server.isJobTracker()) {
                return server;
            }
        }
        return null;
    }

    public Server getSecondaryNameNode() {
        for (Server server : this) {
            if (server.isSecondaryNameNode()) {
                return server;
            }
        }
        return null;
    }

    /**
     * Machines are numbered 0, 1, ..., N Three cluster sizes: 1 2-10 >10
     */
    public void assignRoles() {
        if (size() == 0) {
            return;
        }
        get(0).setNameNode(true);
        if (size() == 1) {
            get(0).setSecondaryNameNode(true);
            get(0).setJobTracker(true);
        } else {
            get(1).setSecondaryNameNode(true);
            get(1).setJobTracker(true);
        }
        if (size() == 1) {
            // for a one-machine cluster this is also the slave
            Server server = get(0);
            server.setDataNode(true);
            server.setTaskTracker(true);
            server.setSlave(true);
        } else if (size() <= 10) {
            // for a larger cluster, 1, 2, ..., N are the slaves
            for (int s = 1; s < size(); ++s) {
                Server server = get(s);
                server.setDataNode(true);
                server.setTaskTracker(true);
                server.setSlave(true);
            }
        } else {
            // for a larger cluster, >10 nodes, 2, 3, ..., N are the slaves
            for (int s = 2; s < size(); ++s) {
                Server server = get(s);
                server.setDataNode(true);
                server.setTaskTracker(true);
                server.setSlave(true);
            }
        }
        History.appendToHistory("Cluster roles assigned:");
        History.appendToHistory(toString());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Server server : this) {
            builder.append(server.isNameNode() ? "NN " : "   ");
        }
        builder.append("\n");
        for (Server server : this) {
            builder.append(server.isJobTracker() ? "JT " : "   ");
        }
        builder.append("\n");
        for (Server server : this) {
            builder.append(server.isSecondaryNameNode() ? "SNN" : "   ");
        }
        builder.append("\n");
        for (Server server : this) {
            builder.append(server.isDataNode() ? "DN " : "   ");
        }
        builder.append("\n");
        for (Server server : this) {
            builder.append(server.isTaskTracker() ? "TT " : "   ");
        }
        builder.append("\n");

        return builder.toString();
    }

    public int getRunningCount() {
        int runningCount = 0;
        for (Server server : this) {
            if (Server.EC2_RUNNING.equals(server.getState())) {
                ++runningCount;
            }
        }
        return runningCount;
    }

    public int getInitializedCount() {
        int init = 0;
        for (Server server : this) {
            if (server.isInitialized()) {
                ++init;
            }
        }
        return init;
    }

    public Cluster getDataNodes() {
        Cluster dataNodeCluster = new Cluster();
        for (Server server : this) {
            if (server.isDataNode()) {
                dataNodeCluster.add(server);
            }
        }
        return dataNodeCluster;
    }

    public Cluster getTaskTrackers() {
        Cluster dataNodeCluster = new Cluster();
        for (Server server : this) {
            if (server.isTaskTracker()) {
                dataNodeCluster.add(server);
            }
        }
        return dataNodeCluster;
    }

    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        this.readyToUse = readyToUse;
    }
}
