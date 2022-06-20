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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.freeeed.search.web.WebConstants;
import org.freeeed.search.web.configuration.Configuration;
import org.freeeed.search.web.model.solr.SolrDocument;
import org.freeeed.search.web.model.solr.SolrResult;
import org.freeeed.search.web.session.SolrSessionObject;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 
 * Class SolrSearch.
 * 
 * Do the actual Solr search.
 * 
 * @author ilazarov.
 * 
 *
 */
public class SolrSearchService {
    private static final Logger log = Logger.getLogger(SolrSearchService.class);
    
    private Configuration configuration;
    private DocumentParser solrDocumentParser;

    /**
     * Search in Solr for the given query.
     * 
     * @param query
     * @param from
     * @param rows
     * @return
     */
    public SolrResult search(String query, int from, int rows) {
        return search(query, from, rows, "gl-search-field", false, null); 
    }        
    
    /**
     * Search in Solr for the given query.
     * 
     * @param query
     * @param from
     * @param rows
     * @param defaultField
     * @param highlight
     * @return
     */
    public SolrResult search(String query, int from, int rows, 
            String defaultField, boolean highlight, String fields) {
        log.debug("Searching: " + query);
        
        String searchResult = searchSolr(query, from, rows, defaultField, highlight, fields);
        
        if (searchResult != null) {
            Document doc = createDOM(searchResult);
            if (doc != null) {
                return buildResult(doc);
            }
        }
        
        return null;
    }
    
    public Set<String> getKeywords(String query, int from, int rows, String defaultField, boolean highlight) {
        Set<String> result = new HashSet<String>();
        
        log.debug("Getting keywords for: " + query);
        
        String searchResult = searchSolr(query, from, rows, defaultField, highlight, null);
        
        if (searchResult != null) {
            Document doc = createDOM(searchResult);
            if (doc != null) {
                return getHighlight(doc);
            }
        }
        
        return result;
    }
    
    private Set<String> getHighlight(Document solrDoc) {
        Set<String> result = new HashSet<String>();
        
        Element root = solrDoc.getDocumentElement();
        NodeList lists = root.getElementsByTagName("lst");
        for (int i = 0; i < lists.getLength(); i++) {
            Element lstEl = (Element) lists.item(i);
            String name = lstEl.getAttribute("name");
            if (("highlighting").equals(name)) {
                NodeList markedStrings = lstEl.getElementsByTagName("str");
                for (int j = 0; j < markedStrings.getLength(); j++) {
                    Element strEl = (Element) markedStrings.item(j);
                    String str = strEl.getTextContent();
                    int start = str.indexOf("<em>");
                    while (start > -1) {
                        int end = str.indexOf("</em>", start);
                        if (end != -1) {
                            String word = str.substring(start + 4, end);
                            result.add(word);
                        }
                        
                        start = str.indexOf("<em>", end);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * Parse the given DOM to SolrResult object.
     * 
     * @param query
     * @param solrDoc
     * @return
     */
    private SolrResult buildResult(Document solrDoc) {
        SolrResult result = new SolrResult();
        
        NodeList responseList = solrDoc.getElementsByTagName("result");
        Element responseEl = (Element) responseList.item(0);
        int totalSize = Integer.parseInt(responseEl.getAttribute("numFound"));
        
        result.setTotalSize(totalSize);
        
        NodeList documentsList = responseEl.getElementsByTagName("doc");
        
        Map<String, SolrDocument> solrDocuments = new HashMap<String, SolrDocument>();
        for (int i = 0; i < documentsList.getLength(); i++) {
            Element documentEl = (Element) documentsList.item(i);
            
            Map<String, List<String>> data = new HashMap<String, List<String>>();
            
            NodeList fieldsList = documentEl.getChildNodes();
            for (int j = 0; j < fieldsList.getLength(); j++) {
                Element field = (Element) fieldsList.item(j);
                String name = field.getAttribute("name");
                List<String> value = new ArrayList<String>();
                
                //multivalues
                if (field.getNodeName().equals("arr")) {
                    NodeList strList = field.getChildNodes();
                    for (int k = 0; k < strList.getLength(); k++) {
                        Node strNode =  strList.item(k);                        
                        value.add(strNode.getTextContent());
                    }
                } else {
                    value.add(field.getTextContent());
                }
                
                data.put(name, value);
            }
            
            SolrDocument doc = solrDocumentParser.createSolrDocument(data);
            solrDocuments.put(doc.getDocumentId(), doc);
        }
        
        result.setDocuments(solrDocuments);
        
        return result;
    }
    
    /**
     * Create DOM element from the received string data.
     * 
     * @param data
     * @return
     */
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
    
    /**
     * 
     * Do the actual HTTP requst to Solr and execute the given query.
     * 
     * @param query
     * @param from
     * @param rows
     * @return
     */
    private String searchSolr(String query, int from, int rows, 
            String defaultField, boolean highlight, String fields) {
        
        HttpServletRequest curRequest = 
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                                                                .getRequest();
        HttpSession session = curRequest.getSession();
        SolrSessionObject solrSession = (SolrSessionObject) 
                            session.getAttribute(WebConstants.WEB_SESSION_SOLR_OBJECT);
        if (solrSession == null || solrSession.getSelectedCase() == null) {
            return null;
        }
        
        String solrCore = solrSession.getSelectedCase().getSolrSourceCore();
        
        if (defaultField == null) {
            defaultField = "gl-search-field";
        }
        
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlStr = configuration.getSolrEndpoint() + 
                                "/solr/" + solrCore + 
                                "/select/?q=" + encodedQuery + "&start=" + from + 
                                "&rows=" + rows + "&df=" + defaultField + "&hl=" + highlight;
            if (fields != null) {
                urlStr += "&fl=" + fields;
            }
            
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

    public void setSolrDocumentParser(DocumentParser solrDocumentParser) {
        this.solrDocumentParser = solrDocumentParser;
    }
    
    public static void main(String args[]) {
        SolrSearchService service = new SolrSearchService();
        Configuration conf = new Configuration();
        service.setConfiguration(conf);
        
        System.out.println(service.getKeywords("ivan mark ester zlati", 0, 10, "gl-search-field", true));
    }
}
