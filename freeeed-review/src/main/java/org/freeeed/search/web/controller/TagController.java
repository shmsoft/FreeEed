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

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.session.SolrSessionObject;
import org.freeeed.search.web.solr.SolrTagService;
import org.freeeed.search.web.solr.SolrTagService.Result;
import org.springframework.web.servlet.ModelAndView;

/**
 * Class TagController.
 * 
 * Tag specific documents with a given tag.
 * 
 * @author ilazarov
 *
 */
public class TagController extends SecureController {
    private static final Logger log = Logger.getLogger(TagController.class);
    
    private SolrTagService solrTagService;

    @Override
    public ModelAndView execute() {
        String action = (String) valueStack.get("action");
        log.debug("Tag action received: " + action);
        
        HttpSession session = this.request.getSession(true);
        SolrSessionObject solrSession = (SolrSessionObject) 
            session.getAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT);
        
        if (solrSession == null) {
            return new ModelAndView(WebConstants.TAG_PAGE);
        }
        
        if ("newtag".equals(action)) {
            String tag = (String) valueStack.get("tag");
            String documentId = (String) valueStack.get("docid");
            
            log.debug("Will do tagging for - documentId: " + documentId + ", tag: " + tag);
            
            if (documentId != null && tag != null && documentId.length() > 0 && tag.trim().length() > 0) {
                Result result = solrTagService.tagDocument(documentId, tag);
                
                valueStack.put("result", result);
            }
        } else if ("deletetag".equals(action)) {
            String tag = (String) valueStack.get("tag");
            String documentId = (String) valueStack.get("docid");
            
            log.debug("Will do delete tag for - documentId: " + documentId + ", tag: " + tag);
            
            if (documentId != null && tag != null && documentId.length() > 0 && tag.trim().length() > 0) {
                Result result = solrTagService.removeTag(documentId, tag);
                
                valueStack.put("result", result);
            }
        } else if ("tagall".equals(action)) {
            String tag = (String) valueStack.get("tag");
            
            log.debug("Will do tag all, tag: " + tag);
            
            if (tag != null && tag.trim().length() > 0) {
                Result result = solrTagService.tagAllDocuments(solrSession, tag);
                
                valueStack.put("result", result);
            }
        } else if ("tagpage".equals(action)) {
            String tag = (String) valueStack.get("tag");
            
            log.debug("Will do tag page, tag: " + tag);
            
            if (tag != null && tag.trim().length() > 0) {
                Result result = solrTagService.tagThisPageDocuments(solrSession, tag);
                
                valueStack.put("result", result);
            }
        }
        
        return new ModelAndView(WebConstants.TAG_PAGE);
    }

    public void setSolrTagService(SolrTagService solrTagService) {
        this.solrTagService = solrTagService;
    }
}
