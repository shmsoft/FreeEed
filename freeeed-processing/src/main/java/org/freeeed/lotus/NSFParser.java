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
package org.freeeed.lotus;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.freeeed.util.OsUtil;
import org.freeeed.services.Settings;


import com.google.gson.Gson;

import de.schlichtherle.io.File;
import de.schlichtherle.io.FileOutputStream;

/**
 * 
 * Class NSFParser.
 * 
 * @author ilazarov.
 *
 */
public class NSFParser {

    public void parseNSF(String nsfFile, String outputDir, Context context) {
        if (OsUtil.isWindows()) {
            try {
                LotusNotesEmailParser parser = new LotusNotesEmailParser(nsfFile, outputDir, null);
                
                System.out.println("Starting local NSF processing...");
                
                parser.parse();
                
                System.out.println("NSF local processing done!");
            } catch (Throwable e) {
                System.out.println("Unable to parse NSF file: " + nsfFile);
                parseExternal(nsfFile, outputDir, context);
            }
        } else {
            parseExternal(nsfFile, outputDir, context);
        }
    }
    
    private void parseExternal(String nsfFile, String outputDir, Context context) {
        String url = Settings.getSettings().getExternalProssingEndpoint();
        if (url == null) {
            System.out.println("External processing machine URL not configured, cannot proceed with the file!");
            return;
        }
        
        System.out.println("Starting external NSF processing...");
        
        String taskId = requestProcessing(url);
        System.out.println("NSF parser, task id received: " + taskId);
        if (taskId == null) {
            return;
        }
        
        SubmitProcessingThread t = new SubmitProcessingThread(url, taskId, nsfFile);
        t.start();
        
        while (!t.finished) {
            try {
                t.join(10000);
            } catch (InterruptedException e) {
            }
            
            if (context != null) {
                context.progress();
            }
        }
        
        boolean sent = t.result;
        System.out.println("NSFParser -- Submit processing status: " + sent);
        if (!sent) {
            return;
        }
        
        System.out.println("NSFParser -- Getting results");
        ProcessingResult result = requestResults(url, taskId);
        while (result.getStatus() == ProcessingStatus.IN_PROGRESS) {
            try {
                Thread.sleep(10000); //10 seconds
            } catch (InterruptedException e) {
            }
            
            if (context != null) {
                context.progress();
            }
            result = requestResults(url, taskId);
        }
        
        if (result.getStatus() == ProcessingStatus.OK) {
            String file = result.getMessage();
            
            System.out.println("NSFParser -- Downloading results");
            
            downloadResults(url, file, outputDir);
        } else {
            System.out.println("NSFParser -- Problem getting result!");
        }
        
        System.out.println("NSF external processing done!");
    }
    
    private boolean downloadResults(String url, String file, String outputDir) {
        HttpClient httpclient = new DefaultHttpClient();
        
        InputStream in = null;
        OutputStream out = null;
        
        try {
            HttpGet httpget = new HttpGet(url + "/result.html?file=" + file);
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                in = entity.getContent();
                out = new BufferedOutputStream(new FileOutputStream(outputDir + File.separator + file));
                
                byte[] buffer = new byte[1024];
                int numRead;
                while ((numRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, numRead);
                }
                
                out.flush();
                
                return true;
            }
        } catch (Exception e) {
            System.out.println("NSFParser -- Problem downloading data: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
        
        return false;
    }
    
    private ProcessingResult requestResults(String url, String taskId) {
        HttpClient httpClient = new DefaultHttpClient();
        
        try {
            HttpGet request = new HttpGet(url + "/check-processing.html?taskId=" + taskId);            
            HttpResponse response = httpClient.execute(request);
            
            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity, "UTF-8");
            
            Gson gson = new Gson();
            return gson.fromJson(responseString, ProcessingResult.class);
            
        } catch (Exception e) {
            System.out.println("NSFParser -- Problem sending data: " + e.getMessage());
        }    
        
        return null;
    }
    
    private boolean submitProcessing(String url, String taskId, String nsfFile) {
        File file = new File(nsfFile);
        
        HttpClient httpClient = new DefaultHttpClient();

        try {
            HttpPost post = new HttpPost(url + "/submit-processing.html");

            MultipartEntity entity = new MultipartEntity();
            entity.addPart("file", new FileBody(file));
            
            entity.addPart("taskId", new StringBody(taskId));
            
            post.setEntity(entity);
            
            HttpResponse response = httpClient.execute(post);
            getResponseMessage(response);
        } catch (Exception e) {
            System.out.println("NSFParser -- Problem sending data: " + e.getMessage());
            
            return false;
        }
        
        return true;
    }
    
    private String getResponseMessage(HttpResponse response) throws ParseException, IOException {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        
        Gson gson = new Gson();
        ProcessingResult pr = gson.fromJson(responseString, ProcessingResult.class);
        if (pr.getStatus() == ProcessingStatus.OK) {
            return pr.getMessage();
        } else {
            System.out.println("Unable to request processing, status: " + pr.getStatus() + " message: " + pr.getMessage());
        }
        
        return null;
    }
    
    private String requestProcessing(String url) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost request = new HttpPost(url + "/request-processing.html");
        
        try {
            HttpResponse response = httpClient.execute(request);
            return getResponseMessage(response);
            
        } catch (Exception e) {
            System.out.println("NSFParser -- Problem sending data: " + e.getMessage());
        }
        
        return null;
    }
    
    public class SubmitProcessingThread extends Thread {
        private String url;
        private String taskId;
        private String nsfFile;
        private boolean result;
        private boolean finished = false;
        
        public SubmitProcessingThread(String url, String taskId, String nsfFile) {
            this.url = url;
            this.taskId = taskId;
            this.nsfFile = nsfFile;
        }
        
        public void run() {
            this.result = submitProcessing(url, taskId, nsfFile);
            this.finished = true;
        }
    }
}
