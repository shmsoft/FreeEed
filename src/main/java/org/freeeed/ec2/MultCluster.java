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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.freeeed.services.Settings;


public class MultCluster {
    private List<Cluster> clusters;
    private Map<String, ServerCluster> serverMap;
    private int clusterSize;
    private Cluster currentCluster;
    
    public MultCluster() {
        this.clusterSize = Settings.getSettings().getClusterSize();
        clusters = new ArrayList<Cluster>();
        serverMap = new HashMap<String, MultCluster.ServerCluster>();
    }
    
    public synchronized void refreshClusters() {     
        EC2Agent agent = new EC2Agent();
        
        Cluster allMachines = agent.getRunningInstances(true);
      
        for (Server server : allMachines) {
            String key = server.getInstanceId();
            ServerCluster sc = serverMap.get(key);
            if (sc != null) {
                sc.server.setPrivateDnsName(server.getPrivateDnsName());
                sc.server.setDnsName(server.getDnsName());
                sc.server.setState(server.getState());
                sc.server.setInitialized(server.isInitialized());
            } else {
                if (currentCluster == null) {
                    currentCluster = new Cluster();
                    clusters.add(currentCluster);
                }
                
                currentCluster.add(server);
                serverMap.put(server.getInstanceId(), new ServerCluster(server, currentCluster));
                if (currentCluster.size() >= clusterSize) {
                    currentCluster = null;
                }
            }
        }
    }
    
    public synchronized List<Cluster> getClusters() {
        return clusters;
    }
    
    private static final class ServerCluster {
        Server server;
        Cluster cluster;
        
        public ServerCluster(Server server, Cluster cluster) {
            this.server = server;
            this.cluster = cluster;
        }
    }
}
