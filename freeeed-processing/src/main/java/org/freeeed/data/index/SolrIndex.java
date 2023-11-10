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
package org.freeeed.data.index;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.util.LogFactory;
import org.slf4j.LoggerFactory;

/**
 *
 * Create Solr index.
 *
 * Currently, implement only creation via HTTP.
 *
 * @author ivanl
 *
 */
public abstract class SolrIndex {
    private final static Logger LOGGER = LogFactory.getLogger(SolrIndex.class.getName());


    public static final String SOLR_INSTANCE_DIR = "shmcloud";
    private static SolrIndex instance;
    protected boolean supportMultipleProjects = true;
    protected boolean supportSolrCloud = false;
    protected String checkedSolrCloudEndpoint = null;
    protected boolean isInited = false;

    public static synchronized SolrIndex getInstance() {
        if (instance == null) {
            if (Project.getCurrentProject().isSendIndexToSolrEnabled()) {
                LOGGER.fine("SolrIndex Create HttpSolrIndex");
                instance = new HttpSolrIndex();
            } else {
                LOGGER.fine("SolrIndex Create DisabledSolrIndex");
                instance = new DisabledSolrIndex();
            }
        }

        return instance;
    }

    public abstract void addData(Metadata metadata);

    public abstract void addBatchData(Metadata metadata);

    public abstract void flushBatchData();

    public abstract void init();

    public void destroy() {
        synchronized (SolrIndex.class) {
            instance = null;
        }
    }

    public boolean isSolrCloud() throws SolrException {
        String endpoint = getSolrEndpoint();
        boolean solrEnabled = Project.getCurrentProject().isSendIndexToSolrEnabled();
        if (solrEnabled && (checkedSolrCloudEndpoint == null || checkedSolrCloudEndpoint.equals(endpoint) == false)) {
            checkedSolrCloudEndpoint = endpoint;
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            HttpClient httpClient = httpClientBuilder.build();
            String command = checkedSolrCloudEndpoint
                    + "/solr/zookeeper?wt=json&detail=false&path=%2Fclusterstate.json";
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
                LOGGER.warning("No SolrCloud found");
            }
        }

        return supportSolrCloud;
    }

    protected void sendPostCommand(String point, String param) throws SolrException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        try {
            HttpPost request = new HttpPost(point);
            StringEntity params = new StringEntity(param, StandardCharsets.UTF_8);
            params.setContentType("text/xml");

            request.setEntity(params);

            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.severe("Solr Invalid Response: " + response.getStatusLine().getStatusCode());
                LOGGER.severe(response.getStatusLine().toString());
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");
                LOGGER.severe(responseString);
                LOGGER.severe("point:");
                LOGGER.severe(point);
                LOGGER.severe("param");
                LOGGER.severe(param);
            }
        } catch (Exception ex) {
            LOGGER.severe("Problem sending request");
        }
    }

    protected void sendGetCommand(String command) throws SolrException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        try {
            HttpGet request = new HttpGet(command);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200) {
                LOGGER.severe("Solr Invalid Response: " + response.getStatusLine().getStatusCode());
                throw new SolrException("Invalid response");
            }
        } catch (IOException | SolrException ex) {
            throw new SolrException("Problem sending request", ex);
        }
    }

    private static final class HttpSolrIndex extends SolrIndex {

        private AtomicLong solrId = new AtomicLong(0);
        private String updateUrl;
        protected StringBuffer batchBuffer = new StringBuffer(1024 * 1024);

        @Override
        public synchronized void addBatchData(Metadata metadata) {
            Settings settings = Settings.getSettings();
            batchBuffer.append("<doc>");
            if (settings.containsKey("mapred.task.id")) {
                String[] idParts = settings.getProperty("mapred.task.id").split("_");
                String taskId = idParts[idParts.length - 2];
                batchBuffer.append("<field name=\"id\">SOLRID_").append(taskId).append("_");
            } else {
                batchBuffer.append("<field name=\"id\">SOLRID_");
            }
            String projectCode = Project.getCurrentProject().getProjectCode();
            batchBuffer.append(projectCode).append("_");
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
            if (batchBuffer.length() > .9 * 1024 * 1024) {
                flushBatchData();
            }
        }

        @Override
        public synchronized void flushBatchData() {
            try {
                if (batchBuffer.length() > 0) {
                    if (updateUrl == null) {
                        if (isInited) {
                            System.err.println("No updateUrl set");
                            batchBuffer.delete(0, batchBuffer.length());
                            return;
                        }
                        resetUpdateUrl();
                    }
                    sendPostCommand(updateUrl, "<add>" + batchBuffer.toString() + "</add>");
                    batchBuffer.delete(0, batchBuffer.length());
                    sendPostCommand(updateUrl, "<commit/>");
                }

            } catch (SolrException e) {
                LOGGER.severe("Error");
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
                StringBuilder param = new StringBuilder();
                param.append("<add>");
                param.append("<doc>");
                if (settings.containsKey("mapred.task.id")) {
                    String[] idParts = settings.getProperty("mapred.task.id").split("_");
                    String taskId = idParts[idParts.length - 2];
                    param.append("<field name=\"id\">SOLRID_").append(taskId).append("_");
                } else {
                    param.append("<field name=\"id\">SOLRID_");
                }
                String projectCode = Project.getCurrentProject().getProjectCode();
                param.append(projectCode).append("_");
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
                LOGGER.severe("Error");
            }
        }

        private String filterNotCorrectCharacters(String data) {
            return data.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "").replaceAll("]]", "");
        }

        @Override
        public void init() {
            isInited = true;
            String command;
            String projectCode = Project.getCurrentProject().getProjectCode();
            String projectName = Project.getCurrentProject().getProjectName();
            try {
                String endpoint = getSolrEndpoint();

                if (isSolrCloud()) {

                    command = endpoint + "solr/admin/collections?action=CREATEALIAS&name=" + projectName.replaceAll("[^A-Za-z0-9]", "_") + "_" + projectCode
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
                        LOGGER.severe("Unable to create Core: " + SOLR_INSTANCE_DIR + "_" + projectCode);
                        LOGGER.severe("Core command: " + command);
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
                LOGGER.severe("Problem with SOLR init");
            }
        }

        protected void resetUpdateUrl() {
            String command = null;
            String projectCode = Project.getCurrentProject().getProjectCode();
            String projectName = Project.getCurrentProject().getProjectName();
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
                LOGGER.severe("Problem with SOLR resetUpdateUrl ");
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
            // do nothing

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
