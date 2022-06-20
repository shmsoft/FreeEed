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

import java.util.List;

import org.freeeed.search.web.model.solr.SolrDocument;
import org.freeeed.search.web.model.solr.SolrEntry;
import org.freeeed.search.web.model.solr.Tag;

/**
 * 
 * Class ResultHighlight.
 * 
 * @author ilazarov
 *
 */
public class ResultHighlight {
    private static final int MAX_COLORS = 10;
    
    /**
     * 
     * Highlight the result for the given keywords.
     * 
     * @param data
     * @param keywords
     */
    public void highlight(SearchResult data, List<YourSearchViewObject> searches) {
        int colorIndex = 0;
        for (int i = 0; i < searches.size(); i++) {
            colorIndex = i + 1;
            if (colorIndex > MAX_COLORS) {
                colorIndex = MAX_COLORS;
            }
            
            YourSearchViewObject querySearch = searches.get(i);
            querySearch.setHighlight(createHightlightClass(colorIndex));
            
            List<String> words = querySearch.getKeywords();
            
            for (String word : words) {
            
                List<SolrDocument> docs = data.getDocuments();
                
                for (SolrDocument solrDocument : docs) {
                    List<SolrEntry> entries = solrDocument.getEntries();
                    for (SolrEntry solrEntry : entries) {
                        String highlighted = highlightResults(solrEntry.getValue(), word, colorIndex);
                        solrEntry.setValue(highlighted);
                    }
                    
                    List<Tag> tags = solrDocument.getTags();
                    for (Tag tag : tags) {
                        String highlighted = highlightResults(tag.getValue(), word, colorIndex);
                        tag.setValue(highlighted);
                    }
                }
            }
        }
    }
    
    /**
     * 
     * Find the given words in the result string and add
     * special html code for special visualization.
     * 
     * @param result
     * @param words
     * @return
     */
    private String highlightResults(String result, String word, int index ) {        
        return result.replaceAll("\\b" + word + "\\b", "<span class='" + createHightlightClass(index) + "'>" + word + "</span>");
    }
    
    private String createHightlightClass(int colorIndex) {
        return "highlight" + colorIndex;
    }
}
