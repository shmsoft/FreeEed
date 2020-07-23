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
package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.mail.MessagingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmlParser;
import org.freeeed.services.ContentTypeMapping;
import org.freeeed.services.JsonParser;
import org.freeeed.services.Util;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is separate to have all Tika-related stuff in a one place It may
 * contain more parsing specifics later on
 */
public class DocumentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParser.class);
    private static final DocumentParser INSTANCE = new DocumentParser();
    private final Tika tika;
    private static final ContentTypeMapping CONTENT_TYPE_MAPPING = new ContentTypeMapping();

    // this is for processing *.jl, will be removed later
    private Mapper.Context context;            // Hadoop processing result context

    public static DocumentParser getInstance() {
        return INSTANCE;
    }

    private DocumentParser() {
        tika = new Tika();
        tika.setMaxStringLength(10 * 1024 * 1024);
    }

    public void parse(DiscoveryFile discoveryFile, DocumentMetadata metadata) {
        LOGGER.debug("Parsing file: {}, original file name: {}", discoveryFile.getPath().getPath(),
                discoveryFile.getRealFileName());
        TikaInputStream inputStream = null;
        try {
            String extension = Util.getExtension(discoveryFile.getRealFileName());
            LOGGER.debug("Detected extension: {}", extension);

            if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(discoveryFile.getPath());
                extractEmlFields(discoveryFile.getPath().getPath(), metadata, emlParser);
                inputStream = TikaInputStream.get(discoveryFile.getPath());
                String text = tika.parseToString(inputStream, metadata);
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
                metadata.setContentType("message/rfc822");
                parseDateTimeReceivedFields(metadata);
                parseDateTimeSentFields(metadata, emlParser.getSentDate());
            } else if ("nsfe".equalsIgnoreCase(extension)) {
                NSFXDataParser emlParser = new NSFXDataParser(discoveryFile.getPath());
                extractEmlFields(discoveryFile.getPath().getPath(), metadata, emlParser);
                metadata.setContentType("application/vnd.lotus-notes");
//            } else if ("jl".equalsIgnoreCase(extension)) {
//                extractJlFields(discoveryFile.getPath().getPath(), metadata);
            } else {
                inputStream = TikaInputStream.get(discoveryFile.getPath());
                metadata.setDocumentText(tika.parseToString(inputStream, metadata));
            }
            String fileType = CONTENT_TYPE_MAPPING.getFileType(metadata.getContentType());
            metadata.setFiletype(fileType);
        } catch (Exception e) {
            // the show must still go on
            LOGGER.info("Exception: " + e.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
            LOGGER.error("Problem parsing file", e);
        } catch (OutOfMemoryError m) {
            LOGGER.error("Out of memory, trying to continue", m);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, m.getMessage());
        } finally {
            // the given input stream is closed by the parseToString method (see Tika documentation)
            // we will close it just in case :) 
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private void parseDateTimeSentFields(DocumentMetadata metadata, Date sentDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String date = df.format(sentDate);
        parseDateTimeFields(metadata, date);
    }

    private void parseDateTimeReceivedFields(DocumentMetadata metadata) {
        String date = metadata.getMessageDate();
        parseDateTimeFields(metadata, date);
    }

    private void parseDateTimeFields(DocumentMetadata metadata, String date) {
        if (date != null && date.length() > 0) {
            try {
                SimpleDateFormat df = null;
                if (date.startsWith("00")) {
                    df = new SimpleDateFormat("'00'yy-MM-dd'T'HH:mm:ss'Z'");
                } else {
                    df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                }

                Date dateObj = df.parse(date);

                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.0+00:00'");
                dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateOnly = dateFormatter.format(dateObj);

                metadata.setMessageDate(dateOnly);
                metadata.setMessageDateSent(dateOnly);
                metadata.setMessageDateReceived(dateOnly);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
                timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeOnly = timeFormatter.format(dateObj);

                metadata.setMessageTimeSent(timeOnly);
                metadata.setMessageTimeReceived(timeOnly);
            } catch (Exception e) {
                LOGGER.error("Problem extracting date time fields" + e.toString());
            }
        }
    }

    /**
     * This function is specifically Memex crawler. *jl means JSON lines.
     * Furthermore, each JSON line has the expected fields
     *
     * @param fileName input file in *jl format
     * @param metadata extracted metadata
     */
    // TODO make the code more elegant, try-with-exceptions
    private void extractJlFields(String fileName, DocumentMetadata metadata) {
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(fileName), "UTF-8");

            while (it.hasNext()) {
                String jsonAsString = it.nextLine();
                String htmlText = JsonParser.getJsonField(jsonAsString, "extracted_text");
                String text = Jsoup.parse(htmlText).text();
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
                metadata.setContentType("application/jl");
            }
        } catch (IOException e) {
            LOGGER.error("Problem with JSON line", e);
        } finally {
            it.close();
        }
    }
    /**
     * Parses JSON given as tech spec
     * 
     * @param jsonLine
     * @param metadata 
     */
    public void parseJsonFields(String jsonLine, DocumentMetadata metadata) {
        Map <String, String> fieldMap = JsonParser.getJsonAsMap(jsonLine);
        Iterator<String>  keyIterator = fieldMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            metadata.addField(key, fieldMap.get(key));
        }
        metadata.setContentType("application/json");
    }

    private void extractEmlFields(String fileName, DocumentMetadata metadata, EmailDataProvider emlParser) {
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
                metadata.setMessageFrom(getAddressLine(emlParser.getFrom()));
            }

            if (emlParser.getSubject() != null) {
                metadata.setMessageSubject(emlParser.getSubject());
            }

            if (emlParser.getMessageId() != null) {
                metadata.setMessageId(emlParser.getMessageId());
            }

            if (emlParser.getReferencedMessageIds() != null) {
                metadata.setReferencedMessageIds(StringUtils.join(emlParser.getReferencedMessageIds(), ", "));
            }

            if (emlParser.getTo() != null) {
                metadata.setMessageTo(getAddressLine(emlParser.getTo()));
            }

            if (emlParser.getCC() != null) {
                metadata.setMessageCC(getAddressLine(emlParser.getCC()));
            }

            if (emlParser.getDate() != null) {
                metadata.setMessageCreationDate(formatDate(emlParser.getDate()));
            }
        } catch (IOException | MessagingException e) {
            LOGGER.error("Problem parsing eml file ", e);
        }
    }

    private static String prepareContent(String content) {
        StringBuilder result = new StringBuilder();

        String[] lines = content.split("\n");
        for (String line : lines) {
            result.append(line.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            result.append("<br/>");
        }

        return result.toString();
    }

    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    private static String getAddressLine(List<String> addresses) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            result.append(address);
            if (i < addresses.size() - 1) {
                result.append(" , ");
            }
        }
        return result.toString();
    }

    /**
     * @return the context
     */
    public Mapper.Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Mapper.Context context) {
        this.context = context;
    }
}
