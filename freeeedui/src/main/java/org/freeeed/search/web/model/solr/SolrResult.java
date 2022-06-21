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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Class SolrResult.
 * 
 * Solr result object.
 * 
 * @author ilazarov.
 *
 */
public class SolrResult implements Cloneable {
    private Map<String, SolrDocument> documents;
    private int totalSize;
    
    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public Map<String, SolrDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, SolrDocument> documents) {
        this.documents = documents;
    }
    
    public SolrResult clone() {
        try {
            SolrResult cloned = (SolrResult) super.clone();
            Map<String, SolrDocument> clonedDocuments = new HashMap<String, SolrDocument>();
            
            for (SolrDocument doc : documents.values()) {
                SolrDocument clonedDoc = doc.clone();
                clonedDocuments.put(clonedDoc.getDocumentId(), clonedDoc);
            }
            
            cloned.documents = clonedDocuments;
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
