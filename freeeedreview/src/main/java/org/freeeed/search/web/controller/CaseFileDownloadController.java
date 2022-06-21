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
package org.freeeed.search.web.controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeeed.search.files.CaseFileService;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.model.Case;
import org.freeeed.search.web.model.solr.SolrDocument;
import org.freeeed.search.web.model.solr.SolrResult;
import org.freeeed.search.web.session.SolrSessionObject;
import org.freeeed.search.web.solr.SolrSearchService;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class CaseFileDownloadController.
 * 
 * @author ilazarov.
 *
 */
public class CaseFileDownloadController extends SecureController {
    private static final Logger log = Logger.getLogger(CaseFileDownloadController.class);
    
    private CaseFileService caseFileService;
    private SolrSearchService searchService;
    
    @Override
    public ModelAndView execute() {
        HttpSession session = this.request.getSession(true);
        SolrSessionObject solrSession = (SolrSessionObject) 
            session.getAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT);
        
        if (solrSession == null || solrSession.getSelectedCase() == null) {
            return new ModelAndView(WebConstants.CASE_FILE_DOWNLOAD);
        }
        
        Case selectedCase = solrSession.getSelectedCase();
        
        String action = (String) valueStack.get("action");
        
        log.debug("Action called: " + action);
        
        File toDownload = null;
        boolean htmlMode = false;
        
        String docPath = (String) valueStack.get("docPath");
        String uniqueId = (String) valueStack.get("uniqueId"); 
        
        try {
            if ("exportNative".equals(action)) {
                toDownload = caseFileService.getNativeFile(selectedCase.getName(), docPath, uniqueId);
                
            } else if ("exportImage".equals(action)) {
                toDownload = caseFileService.getImageFile(selectedCase.getName(), docPath, uniqueId);
            } else if ("exportHtml".equals(action)) {
                toDownload = caseFileService.getHtmlFile(selectedCase.getName(), docPath, uniqueId);
                htmlMode = true;
            } else if ("exportHtmlImage".equals(action)) {
                toDownload = caseFileService.getHtmlImageFile(selectedCase.getName(), docPath);
                htmlMode = true;
            } else if ("exportNativeAll".equals(action)) {
                String query = solrSession.buildSearchQuery();
                int rows = solrSession.getTotalDocuments();
                            
                List<SolrDocument> docs = getDocumentPaths(query, 0, rows);
                
                toDownload = caseFileService.getNativeFiles(selectedCase.getName(), docs);
                
            } else if ("exportNativeAllFromSource".equals(action)) {
                String query = solrSession.buildSearchQuery();
                int rows = solrSession.getTotalDocuments();
                
                List<SolrDocument> docs = getDocumentPaths(query, 0, rows);
                
                String source = (String) valueStack.get("source");
                try {
                    source = URLDecoder.decode(source, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                }
                
                toDownload = caseFileService.getNativeFilesFromSource(source, docs);                
            } else if ("exportImageAll".equals(action)) {
                String query = solrSession.buildSearchQuery();
                int rows = solrSession.getTotalDocuments();
                
                List<SolrDocument> docs = getDocumentPaths(query, 0, rows);
                toDownload = caseFileService.getImageFiles(selectedCase.getName(), docs);
            }
        } catch (Exception e) {
            log.error("Problem sending cotent", e);
            valueStack.put("error", true);
        }
        
        if (toDownload != null) {
            try {
                int length = 0;
                ServletOutputStream outStream = response.getOutputStream();
                String mimetype = "application/octet-stream";
                if (htmlMode) {
                    mimetype = "text/html";
                }
    
                response.setContentType(mimetype);
                response.setContentLength((int) toDownload.length());
                String fileName = toDownload.getName();
    
                if (!htmlMode) {
                    // sets HTTP header
                    response.setHeader("Content-Disposition", "attachment; filename=\""
                            + fileName + "\"");
                }
    
                byte[] byteBuffer = new byte[1024];
                DataInputStream in = new DataInputStream(new FileInputStream(
                        toDownload));
    
                // reads the file's bytes and writes them to the response stream
                while ((in != null) && ((length = in.read(byteBuffer)) != -1)) {
                    outStream.write(byteBuffer, 0, length);
                }
    
                in.close();
                outStream.close();
            } catch (Exception e) {
                log.error("Problem sending cotent", e);
                valueStack.put("error", true);
            }
        } else {
            valueStack.put("error", true);
        }
        
        return new ModelAndView(WebConstants.CASE_FILE_DOWNLOAD);
    }

    private List<SolrDocument> getDocumentPaths(String query, int from, int rows) {
        SolrResult solrResult = searchService.search(query, from, rows, null, false, "id,document_original_path,UPI");
        List<SolrDocument> result = new ArrayList<SolrDocument>(solrResult.getTotalSize());
        result.addAll(solrResult.getDocuments().values());
        return result;
    }
    
    public void setCaseFileService(CaseFileService caseFileService) {
        this.caseFileService = caseFileService;
    }
    
    public void setSearchService(SolrSearchService searchService) {
        this.searchService = searchService;
    }
}
