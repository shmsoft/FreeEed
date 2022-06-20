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
package org.freeeed.search.web.solr;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Class KeywordQuerySearch.
 * Represent a Solr search by query.
 * 
 * @author ivanl
 *
 */
public class KeywordQuerySearch implements QuerySearch {
    private String query;
    private SolrSearchService solrSearchService;
    private int from;
    private int rows;
    
    public KeywordQuerySearch(String query, SolrSearchService solrSearchService,
            int from, int rows) {
        this.query = query;
        this.solrSearchService = solrSearchService;
        this.from = from;
        this.rows = rows;
    }
    
    @Override
    public List<String> getSearchKeywords() {
        List<String> result = new ArrayList<String>();
        result.addAll(solrSearchService.getKeywords(query, from, rows, "gl-search-field", true));
        
        return result;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public void adjust(int from, int rows) {
        this.from = from;
        this.rows = rows;
    }

    @Override
    public String getDisplay() {
        return "Keyword: " + query;
    }

}
