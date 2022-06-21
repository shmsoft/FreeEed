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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.freeeed.search.web.configuration.Configuration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * Class SolrCoreService.
 * 
 * @author ilazarov.
 *
 */
public class SolrCoreService {
    private static final Logger log = Logger.getLogger(SolrCoreService.class);
    
    private Configuration configuration;
    
    /**
     * 
     * Return all core available in Solr.
     * 
     * @return
     */
    public List<String> getSolrCores() {
        List<String> result = new ArrayList<String>();
        
        String data = requestSolrCores();
        if (data == null) {
            return result;
        }
        
        Document dom = createDOM(data);
        
        Element root = dom.getDocumentElement();
        NodeList lists = root.getElementsByTagName("lst");
        for (int i = 0; i < lists.getLength(); i++) {
            Element lstEl = (Element) lists.item(i);
            String name = lstEl.getAttribute("name");
            if (("status").equals(name)) {
                NodeList coreNodes = lstEl.getChildNodes();
                for (int j = 0; j < coreNodes.getLength(); j++) {
                    Element coreEl = (Element) coreNodes.item(j);
                    String core = coreEl.getAttribute("name");
                    result.add(core);
                }
            }
        }
        
        return result;
    }
    
    private Document createDOM(String data) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputSource source = new InputSource(new StringReader(data));
        try {
            return factory.newDocumentBuilder().parse(source);
        } catch (Exception e) {
            log.error("", e);
        } 
        
        return null;
    }
    
    private String requestSolrCores() {
        try {
            String urlStr = configuration.getSolrEndpoint() + 
                                "/solr/admin/cores?action=STATUS";            
            URL url = new URL(urlStr);
            
            log.debug("Will execute: " + url.toString());
            
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            
            StringBuffer resultBuff = new StringBuffer();
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                resultBuff.append(inputLine).append("\n");
            }
                
            in.close();
            
            return resultBuff.toString();
        } catch (Exception e) {
            log.error("Problem accessing Solr: ", e);
        }
        
        return null;
    }
    
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
