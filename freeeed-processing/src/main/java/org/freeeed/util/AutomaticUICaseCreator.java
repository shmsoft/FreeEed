package org.freeeed.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutomaticUICaseCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticUICaseCreator.class);
    
    public CaseInfo createUICase() {
        LOGGER.debug("Preparing to create a case in FreeEedUI...");

        Project project = Project.getCurrentProject();
        String url = !project.isCLI() ? Settings.getSettings().getReviewEndpoint() + "/usercase.html" :
            project.getReviewEndpoint() + "/usercase.html";
        LOGGER.debug("Will submit to this url: {}", url);

        
        String action = "save";
        String caseName = "case_" + project.getProjectCode();
        String caseDescription = project.getProjectName();
        String solrsource = SolrIndex.SOLR_INSTANCE_DIR + "_" + project.getProjectCode();
        String projectId = project.getProjectCode();
        String nativeZipFileRelative = project.getResultsDir();
        File nativeZipFile = new File(nativeZipFileRelative);
        
        String filesLocation = nativeZipFile.getAbsolutePath();
        
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("action", action));
        urlParameters.add(new BasicNameValuePair("name", caseName));
        urlParameters.add(new BasicNameValuePair("description", caseDescription));
        urlParameters.add(new BasicNameValuePair("projectId", projectId));
        urlParameters.add(new BasicNameValuePair("solrsource", solrsource));
        urlParameters.add(new BasicNameValuePair("filesLocation", filesLocation));
        urlParameters.add(new BasicNameValuePair("removecasecreation", "yes"));
        
        LOGGER.debug("Sending to url: {}, name: {}, solr core: {}, file: {}",
                url, caseName, solrsource, filesLocation);
        sendCase(url, urlParameters);
        
        CaseInfo info = new CaseInfo();
        info.setCaseName(caseName);
        return info;
    }
    
    private boolean sendCase(String url, List<NameValuePair> urlParameters) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        try {
            HttpPost request = new HttpPost(url);
            request.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 302) {
                LOGGER.error("Invalid Response: {}", response.getStatusLine().getStatusCode());
                return false;
            }

            return true;
        } catch (Exception ex) {
            LOGGER.error("Problem sending request", ex);
        }
        
        return false;
    }
    
    public static final class CaseInfo {
        private String caseName;

        public String getCaseName() {
            return caseName;
        }

        public void setCaseName(String caseName) {
            this.caseName = caseName;
        }
        
    }
}
