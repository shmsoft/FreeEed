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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
//import org.apache.tika.Tika;
//import org.apache.tika.io.TikaInputStream;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.html.HtmlParser;
//import org.apache.tika.sax.BodyContentHandler;
import org.freeeed.api.tika.RestApiTika;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmlParser;
import org.freeeed.services.ContentTypeMapping;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is separate to have all Tika-related stuff in a one place It may
 * contain more parsing specifics later on
 */
public class DocumentParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParser.class);
    private static final DocumentParser INSTANCE = new DocumentParser();
//    private final Tika tika;
    private static final ContentTypeMapping CONTENT_TYPE_MAPPING = new ContentTypeMapping();

    public static DocumentParser getInstance() {
        return INSTANCE;
    }

//    private DocumentParser() {
//        tika = new Tika();
//        tika.setMaxStringLength(10 * 1024 * 1024);
//    }

    public void parse(DiscoveryFile discoveryFile, DocumentMetadata documentMetadata) {
        LOGGER.debug("Parsing file: {}, original file name: {}", discoveryFile.getPath().getPath(),
                discoveryFile.getRealFileName());
        try {
            String extension = Util.getExtension(discoveryFile.getRealFileName());
            LOGGER.debug("Detected extension: {}", extension);

            if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(discoveryFile.getPath());
                extractEmlFields(discoveryFile.getPath().getPath(), documentMetadata, emlParser);
//                inputStream = TikaInputStream.get(discoveryFile.getPath().toURI());
                RestApiTika tikaServer = new RestApiTika();
                String text = tikaServer.getText(discoveryFile.getPath());
                //TODO: Parse metadata from hashtable
                // TODO do something with the metadata fields that we got
                Hashtable<String, String> metadata = tikaServer.getMetadata(discoveryFile.getPath());
                // TODO copy metadata fields to documentMetadata
                documentMetadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
                documentMetadata.setContentType("message/rfc822");
                parseDateTimeReceivedFields(documentMetadata);
                parseDateTimeSentFields(documentMetadata, emlParser.getSentDate());

            } else if ("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
//                HtmlParser htmlParser = new HtmlParser();
//                ParseContext pcontext = new ParseContext();
//                inputStream = TikaInputStream.get(discoveryFile.getPath().toURI());
//                BodyContentHandler handler = new BodyContentHandler();
//                htmlParser.parse(inputStream, handler, documentMetadata, pcontext);
//                documentMetadata.setDocumentText(handler.toString());
            } else {
                RestApiTika tikaServer = new RestApiTika();
                String text = tikaServer.getText(discoveryFile.getPath());
                documentMetadata.setDocumentText(text);
            }
            String fileType = CONTENT_TYPE_MAPPING.getFileType(documentMetadata.getContentType());
            documentMetadata.setFiletype(fileType);
        } catch (Exception e) {
            // the show must still go on
            LOGGER.info("Exception: " + e.getMessage());
            documentMetadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
            LOGGER.error("Problem parsing file", e);
        } catch (OutOfMemoryError m) {
            LOGGER.error("Out of memory, trying to continue", m);
            documentMetadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, m.getMessage());
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
}
