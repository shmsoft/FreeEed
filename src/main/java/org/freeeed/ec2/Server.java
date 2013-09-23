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

/**
 *
 * @author mark
 */
public class Server {

    private String url;
    private String instanceId;
    private String state;
    private String dnsName;
    private String privateDnsName;
    // usage
    private boolean nameNode;
    private boolean secondaryNameNode;
    private boolean jobTracker;
    private boolean taskTracker;
    private boolean dataNode;
    private boolean slave;
    // Amazon consts
    public static final String EC2_STOPPED = "stopped";
    public static final String EC2_TERMINATED = "terminated";
    public static final String EC2_RUNNING = "running";

    private boolean up;
    private boolean initialized;
    // for initialization checking
    private Thread checkerThread;
    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the instanceId
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * @param instanceId the instanceId to set
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the dnsName
     */
    public String getDnsName() {
        return dnsName;
    }

    /**
     * @param dnsName the dnsName to set
     */
    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * @return the privateDnsName
     */
    public String getPrivateDnsName() {
        // convert the name to number ip, this is how Hadoop likes it better
        // return dnsToIp(privateDnsName);
        return privateDnsName;
    }
    private String dnsToIp(String dns) {
        int dash = dns.indexOf("-");
        if (dash < 0) {
            System.out.println("WARN no dash in DNS name" + dns);
            return "";            
        }
        int dot = dns.indexOf(".", dash);
        if (dot < 0) {
            System.out.println("WARN no dot in DNS name" + dns);
            return "";
        }
        if (dash + 1 > dot) {
            System.out.println("WARN dash, dot in the wrong places" + dns);
            return "";
        }        
        String ip = dns.substring(dash + 1, dot);
        return ip.replaceAll("-", ".");
    }

    /**
     * @param privateDnsName the privateDnsName to set
     */
    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
    }

    /**
     * @return the nameNode
     */
    public boolean isNameNode() {
        return nameNode;
    }

    /**
     * @param nameNode the nameNode to set
     */
    public void setNameNode(boolean nameNode) {
        this.nameNode = nameNode;
    }

    /**
     * @return the secondaryNameNode
     */
    public boolean isSecondaryNameNode() {
        return secondaryNameNode;
    }

    /**
     * @param secondaryNameNode the secondaryNameNode to set
     */
    public void setSecondaryNameNode(boolean secondaryNameNode) {
        this.secondaryNameNode = secondaryNameNode;
    }

    /**
     * @return the jobTracker
     */
    public boolean isJobTracker() {
        return jobTracker;
    }

    /**
     * @param jobTracker the jobTracker to set
     */
    public void setJobTracker(boolean jobTracker) {
        this.jobTracker = jobTracker;
    }

    /**
     * @return the taskTracker
     */
    public boolean isTaskTracker() {
        return taskTracker;
    }

    /**
     * @param taskTracker the taskTracker to set
     */
    public void setTaskTracker(boolean taskTracker) {
        this.taskTracker = taskTracker;
    }

    /**
     * @return the dataNode
     */
    public boolean isDataNode() {
        return dataNode;
    }

    /**
     * @param dataNode the dataNode to set
     */
    public void setDataNode(boolean dataNode) {
        this.dataNode = dataNode;
    }

    /**
     * @return the slave
     */
    public boolean isSlave() {
        return slave;
    }

    /**
     * @param slave the slave to set
     */
    public void setSlave(boolean slave) {
        this.slave = slave;
    }

    /**
     * @return the up
     */
    public boolean isUp() {
        return up;
    }

    /**
     * @param up the up to set
     */
    public void setUp(boolean up) {
        this.up = up;
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * @param initialized the initialized to set
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
    public String getInitialized() {
        if (EC2_RUNNING.equals(getState()))
            return ", " + (isInitialized() ? "initialized" : "initializing");
        else 
            return "";
    }

    /**
     * @return the checkerThread
     */
    public Thread getCheckerThread() {
        return checkerThread;
    }

    /**
     * @param checkerThread the checkerThread to set
     */
    public void setCheckerThread(Thread checkerThread) {
        this.checkerThread = checkerThread;
    }
    @Override
    public String toString() {
        return getDnsName();
    }
}

