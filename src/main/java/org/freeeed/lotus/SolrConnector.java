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
package org.freeeed.lotus;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

public class SolrConnector {
    private static final String SOLR_INSTANCE_DIR = "shmcloud";
    
    private String endpoint;
    private String core;
    private static AtomicLong solrId = new AtomicLong(0);
    private String updateUrl;
    
    public SolrConnector(String endpoint, String core) throws Exception {
        this.endpoint = endpoint;
        this.core = core;
        
        init();
    }
    
    protected void sendPostCommand(String point, String param) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        
        HttpPost request = new HttpPost(point);
        StringEntity params = new StringEntity(param, HTTP.UTF_8);
        params.setContentType("text/xml");
        
        request.setEntity(params);
        
        HttpResponse response = httpClient.execute(request);
        response.getStatusLine().getStatusCode();           
    }
    
    protected void sendGetCommand(String command) throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        
        HttpGet request = new HttpGet(command);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Invalid response");
        }           
    }
    
    public void addData(Map<String, String> data) {
        if (updateUrl == null) {
            return;
        }
        
        StringBuffer param = new StringBuffer();
        param.append("<add>");
        param.append("<doc>");
        param.append("<field name=\"id\">SOLRID");
        param.append(solrId.incrementAndGet());
        param.append("</field>");
        
        Set<String> metadataNames = data.keySet();
        for (String name : metadataNames) {
            String value = data.get(name);
            param.append("<field name=\"");
            param.append(name);
            param.append("\">");
            param.append("<![CDATA[");
            param.append(filterNotCorrectCharacters(value));
            param.append("]]></field>");
        }
        
        param.append("</doc></add>");
        
        try {
            sendPostCommand(updateUrl, param.toString());
            sendPostCommand(updateUrl, "<commit/>");
        } catch (Exception e) {
            System.out.println("Problem sending data: " + e.getMessage());
        }
    }

    private String filterNotCorrectCharacters(String data) {
        return data.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
    }
        
    public void init() throws Exception {
        try {
            String command = endpoint + "solr/admin/cores?action=CREATE&name=" + SOLR_INSTANCE_DIR + "_" + core 
                                + "&instanceDir=" + SOLR_INSTANCE_DIR 
                                + "&config=solrconfig.xml&dataDir=data_" + core
                                + "&schema=schema.xml";
            sendGetCommand(command);
            
            this.updateUrl = endpoint + "solr/" + SOLR_INSTANCE_DIR + "_" + core + "/update";
        
            String deleteAll = "<delete><query>id:[*TO *]</query></delete>";
            sendPostCommand(updateUrl, deleteAll);
            sendPostCommand(updateUrl, "<commit/>");
        } catch (Exception e) {
            System.out.println("Problem sending data: " + e.getMessage());
            throw e;
        }
    }
}
