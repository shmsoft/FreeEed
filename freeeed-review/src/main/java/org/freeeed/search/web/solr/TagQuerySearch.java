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
 * Class TagQuerySearch.
 * 
 * Search by tag.
 * 
 * @author ilazarov.
 *
 */
public class TagQuerySearch implements QuerySearch {
    private String tag;
    
    public TagQuerySearch(String tag) {
        this.tag = tag;
    }
    
    @Override
    public List<String> getSearchKeywords() {
        List<String> result = new ArrayList<String>(1);
        result.add(tag);
        
        return result;
    }

    @Override
    public String getQuery() {
        return "tags-search-field:\"" + tag + "\"";
    }

    @Override
    public void adjust(int from, int rows) {
    }

    @Override
    public String getDisplay() {
        return "Tag: " + tag;
    }
}
