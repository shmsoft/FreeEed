/*    
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
package org.freeeed.lotus;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.RichTextItem;
import lotus.domino.Session;
import lotus.domino.View;

/**
 * 
 * Class LotusNotesEmailParser.
 * 
 * @author ilazarov.
 *
 */
public class LotusNotesEmailParser {
    private static final SimpleDateFormat df1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private FileStorage fileStorage;
    private SolrConnector solrConnector;
    private String lotusDominoEmailDBFile;
    
    public LotusNotesEmailParser(String nsfFile, String outputDir, String solrEndpoint) {
        fileStorage = new FileStorage(outputDir, nsfFile);
        lotusDominoEmailDBFile = nsfFile;
        
        File nsf = new File(nsfFile);
        String name = nsf.getName();
        if (solrEndpoint != null) {
            try {
                solrConnector = new SolrConnector(solrEndpoint, name.substring(0, name.lastIndexOf(".")));
            } catch (Exception e) {
                System.out.println("Solr connector not initialized: " + e.getMessage());
            }
        }
    }
    
    public void parse() {
        NotesConnectorThread notesConnector = new NotesConnectorThread(lotusDominoEmailDBFile);
        
        System.out.println("Lotus notes parser -- Start parsing: " + lotusDominoEmailDBFile);
        notesConnector.start();
        
        try {
            notesConnector.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        if (!notesConnector.success) {
            throw new RuntimeException(notesConnector.caughtException);
        }
    }
    
    private final class NotesConnectorThread extends NotesThread {
        private String nsfFile;
        private boolean success = false;
        private Throwable caughtException;
        
        public NotesConnectorThread(String nsfFile) {
            this.nsfFile = nsfFile;
        }
        
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void runNotes() {
            try {
                Session s = NotesFactory.createSession();
                Database db = s.getDatabase("", nsfFile);
                View vw = db.getView("By Person");
                
                Document doc = vw.getFirstDocument();
                while (doc != null) {
                    LotusEmail le = new LotusEmail();
                    
                    le.setSubject(doc.getItemValueString("Subject"));
                    
                    List<String> from = new ArrayList<String>();
                    from.addAll(doc.getItemValue("From"));
                    le.setFrom(from);
                    
                    List<String> to = new ArrayList<String>();
                    to.addAll(doc.getItemValue("SendTo"));
                    le.setTo(to);
                    
                    List<String> cc = new ArrayList<String>();
                    cc.addAll(doc.getItemValue("CopyTo"));
                    le.setCc(cc);
                    
                    List<String> bcc = new ArrayList<String>();
                    bcc.addAll(doc.getItemValue("BlindCopyTo"));
                    le.setBcc(bcc);

                    try {
                        Vector dateVector = doc.getItemValueDateTimeArray("PostedDate");
                        if (dateVector.size() > 0) {
                            Date d = null;
                            try {
                                d = df1.parse(dateVector.get(0).toString());
                            } catch (Exception e) {
                                d = df2.parse(dateVector.get(0).toString());
                            }
                            
                            if (d != null) {
                                le.setDate(d);
                            }
                        }
                    } catch (Exception e) {
                    }
                    
                    RichTextItem body = (RichTextItem)doc.getFirstItem("Body");
                    le.setContent(body.getText());
                    
                    List<String> attachments = new ArrayList<String>();
                    
                    Vector v = body.getEmbeddedObjects();
                    Enumeration e = v.elements();
                    while (e.hasMoreElements()) {
                        EmbeddedObject eo = (EmbeddedObject) e.nextElement();
                        if (eo.getType() == EmbeddedObject.EMBED_ATTACHMENT) {
                            attachments.add(eo.getName());
                        }
                    }
                    le.setAttachments(attachments);
                    fileStorage.storeEmail(le);
                    sendToSolr(le);
                    
                    doc = vw.getNextDocument(doc);
                }
                
                success = true;
            } catch (Throwable e) {
                System.out.println("ERROR LotusNotesEmailParser -- fail NSF parsing: " + e.getMessage());
                caughtException = e;
                success = false;
            }
        }
    }
    
    private void sendToSolr(LotusEmail email) {
        if (solrConnector != null) {
            Map<String, String> metadata = new HashMap<String, String>();
            
            String text = prepareContent(email.getContent());
            List<String> attachments = email.getAttachmentNames();
            if (attachments.size() > 0) {
                text += "<br/>=====================================<br/>Attachments:<br/><br/>";
                
                for (String att : attachments) {
                    if (att != null) {
                        text += att + "<br/>";
                    }
                }
            }
            
            metadata.put("text", text);
            if (email.getFrom() != null) {
                metadata.put("Message-From", getAddressLine(email.getFrom()));
            }
            
            if (email.getSubject() != null) {
                metadata.put("subject", email.getSubject());
            }
            
            if (email.getTo() != null) {
                metadata.put("Message-To", getAddressLine(email.getTo()));
            }
            
            if (email.getCC() != null) {
                metadata.put("Message-Cc", getAddressLine(email.getCC()));
            }
            
            if (email.getDate() != null) {
                metadata.put("Creation-Date", formatDate(email.getDate()));
            }
            
            solrConnector.addData(metadata);
        }
    }
    
    private static String prepareContent(String content) {
        StringBuffer result = new StringBuffer();
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            result.append(line.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            result.append("<br/>");
        }
        
        return result.toString();
    }
    
    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
    private static String getAddressLine(List<String> addresses) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            result.append(address);
            if (i < addresses.size() - 1) {
                result.append(" , ");
            }
        }
        
        return result.toString();
    }
}
