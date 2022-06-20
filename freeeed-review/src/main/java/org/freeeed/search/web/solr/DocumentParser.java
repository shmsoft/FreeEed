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
import java.util.Map;
import java.util.Set;

import org.freeeed.search.web.model.solr.SolrDocument;
import org.freeeed.search.web.model.solr.SolrEntry;
import org.freeeed.search.web.model.solr.Tag;

/**
 * 
 * Class DocumentParser.
 * 
 * @author ilazarov
 *
 */
public class DocumentParser {

    /**
     * Create a solr document object based
     * on the provided data. The data is sorl document fields
     * in key -> value format.
     * 
     * @param data
     * @return
     */
    public SolrDocument createSolrDocument(Map<String, List<String>> data) {
        SolrDocument doc = new SolrDocument();
        List<SolrEntry> entries = new ArrayList<SolrEntry>();
        
        String documentId = "";
        String from = "";
        String subject = "";
        String date = "";
        String docPath = "";
        String uniqueId = "";
        List<Tag> tags = new ArrayList<Tag>();
        
        Set<Map.Entry<String, List<String>>> entrySet = data.entrySet();
        for (Map.Entry<String, List<String>> entry : entrySet) {
            String name = entry.getKey();
            List<String> allValues = entry.getValue();
            String value = allValues.size() > 0 ? allValues.get(0) : null;
            
            SolrEntry solrEntry = new SolrEntry();
            solrEntry.setKey(name);
            solrEntry.setValue(value);
            
            entries.add(solrEntry);
            
            if ("id".equalsIgnoreCase(name)) {
                documentId = value;
            }
            
            // if ("unique_id".equalsIgnoreCase(name)) {
            if ("UPI".equalsIgnoreCase(name)) {
                uniqueId = value;
            }
            
            if ("creator".equalsIgnoreCase(name)) {
                from = value;
            }
            
            if ("Message-From".equalsIgnoreCase(name)) {
                from = value;
            }
            
            if ("Last-Author".equalsIgnoreCase(name)) {
                from = value;
            }
            
            if ("Author".equalsIgnoreCase(name)) {
                from = value;
            }
            
            if ("document_original_path".equalsIgnoreCase(name)) {
                docPath = value;
                if (subject == null || subject.length() == 0) {
                    subject = value;
                }
            }
            
            if ("subject".equalsIgnoreCase(name)) {
                subject = value;
            }
            
            if ("date".equalsIgnoreCase(name) || "Creation-Date".equalsIgnoreCase(name)) {
                int timeIndex = value.indexOf("T");
                if (timeIndex != -1) {
                    date = value.substring(0, timeIndex);
                } else {
                    date = value;
                }
            }
            
            if ("tags-search-field".equalsIgnoreCase(name)) {
                for (String tagStr : allValues) {
                    Tag tag = new Tag();
                    tag.setValue(tagStr);
                    tag.setName(tagStr);
                    tag.setId(tags.size() + 1);
                    
                    tags.add(tag);                    
                }
            }
        }
        
        doc.setDocumentId(documentId);
        doc.setFrom(from);
        doc.setSubject(subject);
        doc.setDate(date);
        doc.setEntries(entries);
        doc.setTags(tags);
        doc.setDocumentPath(docPath);
        doc.setUniqueId(uniqueId);
        
        return doc;
    }
    
}
