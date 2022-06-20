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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.configuration.Configuration;
import org.freeeed.search.web.model.solr.SolrResult;
import org.freeeed.search.web.session.SolrSessionObject;
import org.freeeed.search.web.solr.KeywordQuerySearch;
import org.freeeed.search.web.solr.QuerySearch;
import org.freeeed.search.web.solr.SolrSearchService;
import org.freeeed.search.web.solr.TagQuerySearch;
import org.freeeed.search.web.view.solr.ResultHighlight;
import org.freeeed.search.web.view.solr.SearchResult;
import org.freeeed.search.web.view.solr.SearchViewPreparer;
import org.freeeed.search.web.view.solr.YourSearchViewObject;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class SearchController.
 * 
 * Implements the search logic.
 * 
 * @author ilazarov
 *
 */
public class SearchController extends SecureController {
    private static final Logger log = Logger.getLogger(SearchController.class);
    
    private Configuration configuration;
    private SolrSearchService solrSearchService;
    private SearchViewPreparer searchViewPreparer;
    private ResultHighlight resultHighlight;
    
    @Override
    public ModelAndView execute() {
        String action = (String) valueStack.get("action");
        log.debug("Search action received: " + action);
        
        HttpSession session = this.request.getSession(true);
        
        SolrSessionObject solrSession = (SolrSessionObject) 
            session.getAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT);
    
        if (solrSession == null) {
            solrSession = new SolrSessionObject();
            session.setAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT, solrSession);
        }
        
        int page = 1;
        int rows = configuration.getNumberOfRows();
        int from = 0;
        
        if ("search".equals(action)) {
            
            //setup the query
            String search = (String) valueStack.get("query");
            if (search != null && search.length() > 0) {
                KeywordQuerySearch qs = new KeywordQuerySearch(search, solrSearchService, from, rows);
                solrSession.addQuery(qs);
            }
            
        } else if ("tagsearch".equals(action)) {
            
            String tag = (String) valueStack.get("tag");
            if (tag != null && tag.length() > 0) {
                TagQuerySearch qs = new TagQuerySearch(tag);
                solrSession.addQuery(qs);
            }
            
        } else if ("remove".equals(action)) {
            String idStr = (String) valueStack.get("id");
            try {
                int id = Integer.parseInt(idStr);
                solrSession.removeById(id);
            } catch (Exception e) {
            }
            
        } else if ("removeall".equals(action)) {
            solrSession.removeAll();
        } else if ("changepage".equals(action)) {
            String pageStr = (String) valueStack.get("page");
            if (pageStr != null) {
                try {
                    page = Integer.parseInt(pageStr);
                    if (page < 1) {
                        page = 1;
                    }
                    
                    if (solrSession != null) {
                        if (page > solrSession.getTotalPage()) {
                            page = solrSession.getTotalPage();
                        }
                    }
                } catch (Exception e) {
                }
                
                from = (page - 1) * configuration.getNumberOfRows();
            }
        }
        
        List<YourSearchViewObject> yourSearches = new ArrayList<YourSearchViewObject>();
        
        List<QuerySearch> searches = solrSession.getQueries();
        for (int i = 0; i < searches.size(); i++) {
            QuerySearch querySearch = searches.get(i);
            querySearch.adjust(from, rows);
            
            YourSearchViewObject so = new YourSearchViewObject();
            so.setId(i + 1);
            so.setName(querySearch.getDisplay());
            so.setKeywords(querySearch.getSearchKeywords());
            
            yourSearches.add(so);
        }
        
        if (searches.size() > 0) {
        
            String search = solrSession.buildSearchQuery();
            
            SolrResult result = solrSearchService.search(search, from, rows);
            //if solr returns correct result
            if (result != null) {
                //prepare the view data
                SearchResult resultView = searchViewPreparer.prepareView(result);
                resultHighlight.highlight(resultView, yourSearches);
                
                valueStack.put("result", resultView);
                valueStack.put("searched", yourSearches);
    
                solrSession.setCurrentPage(page);
                
                int total = result.getTotalSize() / configuration.getNumberOfRows();
                if (result.getTotalSize() % configuration.getNumberOfRows() > 0) {
                    total ++;
                }
                
                solrSession.setTotalPage(total);
                solrSession.setTotalDocuments(result.getTotalSize());
                                
                setupPagination();
            }
        }
        
        return new ModelAndView(WebConstants.SEARCH_AJAX_PAGE);
    }
    
    private void setupPagination() {
        SolrSessionObject session = (SolrSessionObject) 
            this.request.getSession(true).getAttribute("solrSession");
        
        valueStack.put("showPagination", session.getTotalPage() > 1);
        valueStack.put("currentPage", session.getCurrentPage());
        valueStack.put("showPrev", session.getCurrentPage() > 1);
        valueStack.put("showNext", session.getCurrentPage() < session.getTotalPage());
        valueStack.put("searchPerformed", true);
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setSolrSearchService(SolrSearchService solrSearchService) {
        this.solrSearchService = solrSearchService;
    }

    public void setSearchViewPreparer(SearchViewPreparer searchViewPreparer) {
        this.searchViewPreparer = searchViewPreparer;
    }

    public void setResultHighlight(ResultHighlight resultHighlight) {
        this.resultHighlight = resultHighlight;
    }
}
