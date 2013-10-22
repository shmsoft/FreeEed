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
package org.freeeed.data.index;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.History;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;

/**
 * 
 * Create Solr index.
 * 
 * Currently implement only creation via HTTP.
 * 
 * @author ivanl
 *
 */
public abstract class SolrIndex implements ComponentLifecycle {
    private static final String SOLR_INSTANCE_DIR = "shmcloud";
    
    private static SolrIndex __instance;
    protected boolean supportMultipleProjects = true;
    protected boolean supportSolrCloud = false;
    protected String checkedSolrCloudEndpoint = null;
    protected boolean isInited = false;
    
    public static synchronized SolrIndex getInstance() {
        if (__instance == null) {
            if (Project.getProject().isSendIndexToSolrEnabled()) {
                System.out.println("SolrIndex Create HttpSolrIndex");
                __instance = new HttpSolrIndex();
            } else {
                System.out.println("SolrIndex Create DisabledSolrIndex");
                __instance = new DisabledSolrIndex();
            }
        }
        
        return __instance;
    }
    
    public abstract void addData(Metadata metadata);
    
    public abstract void addBatchData(Metadata metadata);
    
    public abstract void flushBatchData();

    @Override
    public abstract void init();

    @Override
    public void destroy() {
        synchronized (SolrIndex.class) {
            __instance = null;
        }
    }
    
    public boolean isSolrCloud() throws SolrException{
        String endpoint = getSolrEndpoint();
        if (checkedSolrCloudEndpoint == null || checkedSolrCloudEndpoint.equals(endpoint) == false){
            checkedSolrCloudEndpoint = endpoint;
            HttpClient httpClient = new DefaultHttpClient();
            
            String command = checkedSolrCloudEndpoint +
            "/solr/zookeeper?wt=json&detail=false&path=%2Fclusterstate.json";
            try {
                HttpGet request = new HttpGet(command);
                HttpResponse response = httpClient.execute(request);
                if (response.getStatusLine().getStatusCode() == 200) {
                    supportSolrCloud = true;
                } else {
                    supportSolrCloud = false;
                }
            } catch (Exception ex) {
                supportSolrCloud = false;
                checkedSolrCloudEndpoint = null;
                throw new SolrException("Problem Checking SolrCloud Status", ex);
            }
        }
        
        return supportSolrCloud;
    }
    
    protected void sendPostCommand(String point, String param) throws SolrException {
        HttpClient httpClient = new DefaultHttpClient();
        
        try {
            HttpPost request = new HttpPost(point);
            StringEntity params = new StringEntity(param, HTTP.UTF_8);
            params.setContentType("text/xml");
            
            request.setEntity(params);
            
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                History.appendToHistory("Solr Invalid Response: " + response.getStatusLine().getStatusCode());
            }

        } catch (Exception ex) {
            throw new SolrException("Problem sending request", ex);
        }           
    }
    
    protected void sendGetCommand(String command) throws SolrException {
        HttpClient httpClient = new DefaultHttpClient();
        
        try {
            HttpGet request = new HttpGet(command);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                History.appendToHistory("Solr Invalid Response: " + response.getStatusLine().getStatusCode());
                 throw new SolrException("Invalid response");
            }
        } catch (Exception ex) {
            throw new SolrException("Problem sending request", ex);
        }           
    }
    
    private static final class HttpSolrIndex extends SolrIndex {
        private static AtomicLong solrId = new AtomicLong(0);
        private String updateUrl;
        protected StringBuffer batchBuffer = new StringBuffer(1024*1024);
        
        @Override
        public synchronized void addBatchData(Metadata metadata) {
            
                Settings settings = Settings.getSettings();
                batchBuffer.append("<doc>");
                if (settings.containsKey("mapred.task.id")) {
                    String[] idParts = settings.getProperty("mapred.task.id").split("_");
                    String taskId = idParts[idParts.length-2];
                    batchBuffer.append("<field name=\"id\">SOLRID_" + taskId + "_");
                } else {
                    batchBuffer.append("<field name=\"id\">SOLRID_");
                }
                String projectCode = Project.getProject().getProjectCode();
                batchBuffer.append(projectCode + "_");
                batchBuffer.append(solrId.incrementAndGet());
                batchBuffer.append("</field>");
                
                String[] metadataNames = metadata.names();
                for (String name : metadataNames) {
                    String data = metadata.get(name);
                    batchBuffer.append("<field name=\"");
                    batchBuffer.append(name);
                    batchBuffer.append("\">");
                    batchBuffer.append("<![CDATA[");
                    batchBuffer.append(filterNotCorrectCharacters(data));
                    batchBuffer.append("]]></field>");
                }
                
                batchBuffer.append("</doc>");
                
                
                if (batchBuffer.length() > .9 * 1024 * 1024){
                    flushBatchData();
                }
        }
        
        @Override
        public synchronized void flushBatchData() {
            try {
                if (batchBuffer.length() > 0){
                    if (updateUrl == null) {
                        if (isInited) {
                            System.err.println("No updateUrl set");
                            batchBuffer.delete(0,batchBuffer.length());
                            return;
                        }
                        resetUpdateUrl();
                    }
                    sendPostCommand(updateUrl, "<add>" + batchBuffer.toString() + "</add>");
                    batchBuffer.delete(0,batchBuffer.length());
                    sendPostCommand(updateUrl, "<commit/>");
                }
            
            } catch (SolrException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void addData(Metadata metadata) {
            if (updateUrl == null) {
                if (isInited) {
                    System.err.println("No updateUrl set");
                    return;
                }
                resetUpdateUrl();
            }
           
            try {
                Settings settings = Settings.getSettings();
                StringBuffer param = new StringBuffer();
                param.append("<add>");
                param.append("<doc>");
                if (settings.containsKey("mapred.task.id")) {
                    String[] idParts = settings.getProperty("mapred.task.id").split("_");
                    String taskId = idParts[idParts.length-2];
                    param.append("<field name=\"id\">SOLRID_" + taskId + "_");
                } else {
                     param.append("<field name=\"id\">SOLRID_");
                }
                String projectCode = Project.getProject().getProjectCode();
                param.append(projectCode + "_");
                param.append(solrId.incrementAndGet());
                param.append("</field>");
                
                String[] metadataNames = metadata.names();
                for (String name : metadataNames) {
                    String data = metadata.get(name);
                    param.append("<field name=\"");
                    param.append(name);
                    param.append("\">");
                    param.append("<![CDATA[");
                    param.append(filterNotCorrectCharacters(data));
                    param.append("]]></field>");
                }
                
                param.append("</doc></add>");
                
                sendPostCommand(updateUrl, param.toString());
                sendPostCommand(updateUrl, "<commit/>");
            } catch (SolrException e) {
                e.printStackTrace();
            }
        }

        private String filterNotCorrectCharacters(String data) {
            return data.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
        }
        
        @Override
        public void init() {
            isInited = true;
            String command = null;
            String projectCode = Project.getProject().getProjectCode();
            String projectName = Project.getProject().getProjectName();
            try {
                String endpoint = getSolrEndpoint();
                
                if (isSolrCloud()) {
                    Settings settings = Settings.getSettings();
                    
                    command = endpoint + "solr/admin/collections?action=CREATE&name=" + SOLR_INSTANCE_DIR + "_" + projectCode
                    + "&collection.configName=" + SOLR_INSTANCE_DIR
					+ "&numShards=" + settings.getSolrCloudShardCount()
					+ "&replicationFactor=" + settings.getSolrCloudReplicaCount()
					+ "&maxShardsPerNode=" + settings.getSolrCloudReplicaCount();
                    try {
                        sendGetCommand(command);
                    } catch (Exception ex) {
                        History.appendToHistory("Unable to create Collection: " + SOLR_INSTANCE_DIR + "_" + projectCode);
                        History.appendToHistory("Collection command: " + command);
                    }
                    
                    command = endpoint + "solr/admin/collections?action=CREATEALIAS&name=" + projectName.replaceAll("[^A-Za-z0-9]","_") + "_" + projectCode
                    + "&collections=" + SOLR_INSTANCE_DIR + "_" + projectCode;
                    sendGetCommand(command);
                    
                    this.updateUrl = endpoint + "solr/" + SOLR_INSTANCE_DIR + "_" + projectCode + "/update";
                } else if (supportMultipleProjects) {
                    command = endpoint + "solr/admin/cores?action=CREATE&name=" + SOLR_INSTANCE_DIR + "_" + projectCode
                    + "&instanceDir=" + SOLR_INSTANCE_DIR
                    + "&config=solrconfig.xml&dataDir=data_" + projectCode
                    + "&schema=schema.xml";
                    try {
                        sendGetCommand(command);
                    } catch (Exception ex) {
                        History.appendToHistory("Unable to create Core: " + SOLR_INSTANCE_DIR + "_" + projectCode);
                        History.appendToHistory("Core command: " + command);
                    }
                    
                    this.updateUrl = endpoint + "solr/" + SOLR_INSTANCE_DIR + "_" + projectCode + "/update";
                } else {
                    sendGetCommand(endpoint + "solr/admin/ping");
                    
                    this.updateUrl = endpoint + "solr/update";
                }
                
                String deleteAll = "<delete><query>id:[*TO *]</query></delete>";
                sendPostCommand(updateUrl, deleteAll);
                sendPostCommand(updateUrl, "<commit/>");
            } catch (SolrException se) {
                History.appendToHistory("Problem with SOLR init: " + se.getMessage());
                //se.printStackTrace();
            }
        }
        
        protected void resetUpdateUrl() {
            String command = null;
            String projectCode = Project.getProject().getProjectCode();
            String projectName = Project.getProject().getProjectName();
            try {
                String endpoint = getSolrEndpoint();
                
                if (isSolrCloud()) {
                    Settings settings = Settings.getSettings();
                    this.updateUrl = endpoint + "solr/" + SOLR_INSTANCE_DIR + "_" + projectCode + "/update";
                } else if (supportMultipleProjects) {
                    this.updateUrl = endpoint + "solr/" + SOLR_INSTANCE_DIR + "_" + projectCode + "/update";
                } else {
                     this.updateUrl = endpoint + "solr/update";
                }
            } catch (SolrException se) {
                History.appendToHistory("Problem with SOLR resetUpdateUrl: " + se.getMessage());
                //se.printStackTrace();
            }
        }
        
    }
    
    protected String getSolrEndpoint() throws SolrException {
        String endpoint = Settings.getSettings().getSolrEndpoint();
        
        if (endpoint == null || endpoint.length() == 0) {
            throw new SolrException("Endpoint not configured");
        }
        
        if (endpoint.endsWith("/")) {
            return endpoint;
        }
      
        return endpoint + "/";
    }
    
    
    private static final class DisabledSolrIndex extends SolrIndex {

        @Override
        public void addData(Metadata metadata) {
            //do nothing
        }
        
        @Override
        public void addBatchData(Metadata metadata) {
            //do nothing
        }
        
        @Override
        public void flushBatchData() {
            //do nothing
        }
        
        @Override
        public void init() {
            //do nothing
        }
    }
    
    private static final class SolrException extends Exception {
        private static final long serialVersionUID = 5904372392164798773L;

        public SolrException(String message) {
            super(message);
        }
        
        public SolrException(String message, Exception e) {
            super(message, e);
        }
    }
}
