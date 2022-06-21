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

import java.util.Iterator;
import java.util.List;

import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.model.Case;
import org.freeeed.search.web.session.SessionContext;
import org.freeeed.search.web.session.SolrSessionObject;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Class TagAutoCompleteController.
 * 
 * @author ilazarov.
 *
 */
public class TagAutoCompleteController extends SecureController {

    @Override
    public ModelAndView execute() {
        SolrSessionObject solrSession = SessionContext.getSolrSession();
        if (solrSession != null) {
            Case c = solrSession.getSelectedCase();
            
            String term = (String) valueStack.get("term");
            if (c != null) {
                List<String> tags = c.getTags();
                Iterator<String> iter = tags.iterator();
                while (iter.hasNext()) {
                    String tag = iter.next();
                    if (!tag.matches(".*" + term + ".*")) {
                        iter.remove();
                    }
                }
                
                StringBuffer result = new StringBuffer();
                result.append("[");
                
                for (int i = 0; i < tags.size(); i++) {
                    String tag = tags.get(i);
                    result.append("\"").append(tag).append("\"");
                    if (i < tags.size() - 1) {
                        result.append(",");
                    }
                }
                
                result.append("]");
                
                valueStack.put("data", result.toString());
            }
        }
        return new ModelAndView(WebConstants.TAG_AUTO);
    }

}
