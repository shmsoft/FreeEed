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

import java.util.List;

import javax.servlet.http.HttpSession;

import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.dao.cases.CaseDao;
import org.freeeed.search.web.model.Case;
import org.freeeed.search.web.session.SolrSessionObject;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class SearchPageController.
 * 
 * Serving the search main page.
 * 
 * @author ilazarov.
 *
 */
public class SearchPageController extends SecureController {
    private CaseDao caseDao;
    
    @Override
    public ModelAndView execute() {
        HttpSession session = this.request.getSession(true);
        
        SolrSessionObject solrSession = (SolrSessionObject) 
            session.getAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT);

        if (solrSession != null) {
            solrSession.reset();
        } else {
            solrSession = new SolrSessionObject();
            session.setAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT, solrSession);
        }

        List<Case> cases = caseDao.listCases();
        valueStack.put("cases", cases);
        
        if (solrSession.getSelectedCase() == null) {
            if (cases.size() > 0) {
                solrSession.setSelectedCase(cases.get(0));
            }
        }
        
        valueStack.put("selectedCase", solrSession.getSelectedCase());
        if (solrSession.getSelectedCase() != null) {
            valueStack.put("tags", solrSession.getSelectedCase().getTags());
        }
        
        String action = (String) valueStack.get("action");
        if ("changecase".equalsIgnoreCase(action)) {
            String caseIdStr = (String) valueStack.get("id");
            try {
                Case selected = caseDao.findCase(Long.parseLong(caseIdStr));
                solrSession.setSelectedCase(selected);
                valueStack.put("selectedCase", solrSession.getSelectedCase());
                valueStack.put("tags", solrSession.getSelectedCase().getTags());
            } catch (Exception e) {
            }
        }
        
        return new ModelAndView(WebConstants.SEARCH_PAGE);
    }

    public void setCaseDao(CaseDao caseDao) {
        this.caseDao = caseDao;
    }
}
