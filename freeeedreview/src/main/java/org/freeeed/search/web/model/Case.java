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
package org.freeeed.search.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * Class Case.
 * 
 * @author ilazarov.
 *
 */
public class Case implements Serializable {
    private static final long serialVersionUID = -6264664386877239067L;
    
    private Long id;
    private String name;
    private String description;
    private Set<String> tags = new HashSet<String>();
    private String filesLocation;
    private String uploadedFile;
    
    private String solrSourceCore;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSolrSourceCore() {
        return solrSourceCore;
    }

    public void setSolrSourceCore(String solrSourceCore) {
        this.solrSourceCore = solrSourceCore;
    }
    
    public String getFilesLocation() {
        return filesLocation;
    }

    public void setFilesLocation(String filesLocation) {
        this.filesLocation = filesLocation;
    }
    
    public String getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(String uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public synchronized void addTag(String tag) {
        if (tags == null) {
            tags = new HashSet<String>();
        }
        
        tags.add(tag);
    }
    
    public synchronized void removeTag(String tag) {
        if (tags != null) {
            tags.remove(tag);
        }
    }
    
    public synchronized List<String> getTags() {
        List<String> result = new ArrayList<String>();
        if (tags != null) {
            result.addAll(tags);   
        }
        
        return result;
    }
}
