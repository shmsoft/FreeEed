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
package org.freeeed.search.web.model.solr;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Class SolrDocument.
 * 
 * Represent one Solr document return by the Solr search.
 * 
 * @author ilazarov.
 *
 */
public class SolrDocument implements Cloneable {
    private String documentId;
    private List<SolrEntry> entries;
    private List<Tag> tags;
    private String from;
    private String subject;
    private String date;
    private String documentPath;
    private String uniqueId;
    
    public List<SolrEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SolrEntry> entries) {
        this.entries = entries;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<Tag> getTags() {
        return tags;
    }
    
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
    
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public SolrDocument clone() {
        try {
            SolrDocument cloned = (SolrDocument) super.clone();
            List<SolrEntry> clonedEntries = new ArrayList<SolrEntry>();
            for (SolrEntry solrEntry : entries) {
                clonedEntries.add(solrEntry.clone());
            }
            
            cloned.entries = clonedEntries;
            
            List<Tag> clonedTags = new ArrayList<Tag>();
            for (Tag tag : tags) {
                clonedTags.add(tag.clone());
            }
            
            cloned.tags = clonedTags;
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
