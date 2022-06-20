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
package org.freeeed.search.web.session;

import java.util.ArrayList;
import java.util.List;

import org.freeeed.search.web.model.Case;
import org.freeeed.search.web.solr.QuerySearch;

/**
 * 
 * Class SolrSessionObject.
 * 
 * Keeps solr related search data in the web session.
 * 
 * @author ilazarov
 *
 */
public class SolrSessionObject {
    private int currentPage;
    private int totalPage;
    private int totalDocuments;
    private List<QuerySearch> queries = new ArrayList<QuerySearch>();
    private Case selectedCase;
    
    public int getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    
    public int getTotalPage() {
        return totalPage;
    }
    
    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }
    
    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public synchronized void addQuery(QuerySearch query) {
        String q = query.getQuery();
        for (QuerySearch qs : queries) {
            if (qs.getQuery().equalsIgnoreCase(q)) {
                return;
            }
        }
        
        queries.add(query);
    }
    
    public synchronized void removeById(int id) {
        if (id >=0 && id < queries.size()) {
            queries.remove(id);
        }
    }
    
    public synchronized void removeAll() {
        queries.clear();
    }
    
    public synchronized List<QuerySearch> getQueries() {
        List<QuerySearch> result = new ArrayList<QuerySearch>();
        result.addAll(queries);
        return result;
    }
    
    public String buildSearchQuery() {
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i < queries.size(); i++) {
            QuerySearch qs = queries.get(i);
            sb.append("(").append(qs.getQuery()).append(")");
            if (i < queries.size() - 1) {
                sb.append(" AND ");
            }
        }
        
        return sb.toString();
    }
    
    public void reset() {
        queries.clear();
        currentPage = 1;
    }

    public Case getSelectedCase() {
        return selectedCase;
    }

    public void setSelectedCase(Case selectedCase) {
        this.selectedCase = selectedCase;
    }
}
