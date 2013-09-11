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
package org.freeeed.main;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmlParser;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.History;


/**
 * This class is separate to have all Tika-related stuff in a one place It may
 * contain more parsing specifics later on
 */
public class DocumentParser {

    private static DocumentParser instance = new DocumentParser();
    private Tika tika;

    public static DocumentParser getInstance() {
        return instance;
    }

    private DocumentParser() {
        tika = new Tika();
        tika.setMaxStringLength(10 * 1024 * 1024);
    }

    public void parse(String fileName, Metadata metadata, String originalFileName) {
        TikaInputStream inputStream = null;
        try {
            String extension = FreeEedUtil.getExtension(originalFileName);
            
            if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(new File(fileName));
                extractEmlFields(fileName, metadata, emlParser);
                
                inputStream = TikaInputStream.get(new File(fileName));
                String text = tika.parseToString(inputStream, metadata);
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
                
                parseDateTimeReceivedFields(metadata);
                parseDateTimeSentFields(metadata, emlParser.getSentDate());
            } else if ("nsfe".equalsIgnoreCase(extension)) {
                NSFXDataParser emlParser = new NSFXDataParser(new File(fileName));
                extractEmlFields(fileName, metadata, emlParser);
            } else {
                // the given input stream is closed by the parseToString method (see Tika documentation)
                // we will close it just in case :)            
                inputStream = TikaInputStream.get(new File(fileName));
                String text = tika.parseToString(inputStream, metadata);
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
            }
            
        } catch (Exception e) {
            // the show must still go on
            History.appendToHistory("Exception: " + e.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
        } catch (OutOfMemoryError m) {
            History.appendToHistory("Memory Exception: " + m.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, m.getMessage());            
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private void parseDateTimeSentFields(Metadata metadata, Date sentDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String date = df.format(sentDate);
        parseDateTimeFields(metadata, date, DocumentMetadataKeys.DATE, 
                DocumentMetadataKeys.DATE_RECEIVED, DocumentMetadataKeys.TIME_RECEIVED);
    }
    
    private void parseDateTimeReceivedFields(Metadata metadata) {
        String date = metadata.get(DocumentMetadataKeys.DATE);
        parseDateTimeFields(metadata, date, DocumentMetadataKeys.DATE, 
                DocumentMetadataKeys.DATE_RECEIVED, DocumentMetadataKeys.TIME_RECEIVED);
    }
    
    private void parseDateTimeFields(Metadata metadata, String date, String key, 
            String dateKey, String timeKey) {
        if (date != null && date.length() > 0) {
            try {
                SimpleDateFormat df = null;
                if (date.startsWith("00")) {
                    df = new SimpleDateFormat("'00'yy-MM-dd'T'HH:mm:ss'Z'");
                } else {
                    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                }
                
                Date dateObj = df.parse(date);
                
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
                String dateOnly = dateFormatter.format(dateObj);
                metadata.set(dateKey, dateOnly);
                metadata.set(key, dateOnly);
                
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                String timeOnly = timeFormatter.format(dateObj);
                metadata.set(timeKey, timeOnly);
            } catch (Exception e) {
            }
        }
    }
    
    private void extractEmlFields(String fileName, Metadata metadata, EmailDataProvider emlParser) {
        try {
            String text = prepareContent(emlParser.getContent());
            List<String> attachments = emlParser.getAttachmentNames();
            if (attachments.size() > 0) {
                text += "<br/>=====================================<br/>Attachments:<br/><br/>";
                
                for (String att : attachments) {
                    if (att != null) {
                        text += att + "<br/>";
                    }
                }
            }
            
            metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
            if (emlParser.getFrom() != null) {
                metadata.set(DocumentMetadataKeys.MESSAGE_FROM, getAddressLine(emlParser.getFrom()));
            }
            
            if (emlParser.getSubject() != null) {
                metadata.set(DocumentMetadataKeys.SUBJECT, emlParser.getSubject());
            }
            
            if (emlParser.getTo() != null) {
                metadata.set(DocumentMetadataKeys.MESSAGE_TO, getAddressLine(emlParser.getTo()));
            }
            
            if (emlParser.getCC() != null) {
                metadata.set(DocumentMetadataKeys.MESSAGE_CC, getAddressLine(emlParser.getCC()));
            }
            
            if (emlParser.getDate() != null) {
                metadata.set(DocumentMetadataKeys.MESSAGE_DATE, formatDate(emlParser.getDate()));
            }
            
        } catch (Exception e) {
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
    
    public static void main(String[] argv) {
        String fileName = "test-data/01-one-time-test/215.eml";
        Metadata metadata = new Metadata();
        getInstance().parse(fileName, metadata, fileName);
        System.out.println(metadata);
    }
}
