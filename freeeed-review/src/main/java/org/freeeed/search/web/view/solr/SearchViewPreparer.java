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
package org.freeeed.search.web.view.solr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.freeeed.search.web.model.solr.SolrDocument;
import org.freeeed.search.web.model.solr.SolrEntry;
import org.freeeed.search.web.model.solr.SolrResult;
import org.freeeed.search.web.model.solr.Tag;

/**
 * 
 * Class SearchViewPreparer.
 * 
 * @author ilazarov
 *
 */
public class SearchViewPreparer {

    /**
     * Prepare the data for output - hide not necessary fields,
     * sort them, etc.
     * 
     * @param data
     * @return
     */
    public SearchResult prepareView(SolrResult data) {
        SolrResult clonedSearch = data.clone();
        
        List<SolrDocument> docs = new ArrayList<SolrDocument>();
        docs.addAll(clonedSearch.getDocuments().values());
        
        SolrEntryComparator entriesComparator = new SolrEntryComparator();
        
        for (SolrDocument solrDocument : docs) {
            List<SolrEntry> entries = solrDocument.getEntries();
            Iterator<SolrEntry> i = entries.iterator();
            while (i.hasNext()) {
                SolrEntry solrEntry = (SolrEntry) i.next();
                if (solrEntry.getKey().startsWith("tags_")
                        || solrEntry.getKey().equalsIgnoreCase("gl-search-field")
                        || solrEntry.getKey().equalsIgnoreCase("tags-search-field")) {
                    i.remove();
                }
            }
            
            Collections.sort(entries, entriesComparator);
            
            Iterator<Tag> tagsIter = solrDocument.getTags().iterator();
            while (tagsIter.hasNext()) {
                Tag tag = (Tag) tagsIter.next();
                if (tag.getValue() == null || tag.getValue().trim().length() == 0) {
                    tagsIter.remove();
                }
            }
        }
        
        SearchResult result = new SearchResult();
        result.setTotalSize(clonedSearch.getTotalSize());
        result.setDocuments(docs);
        
        return result;
    }
}
