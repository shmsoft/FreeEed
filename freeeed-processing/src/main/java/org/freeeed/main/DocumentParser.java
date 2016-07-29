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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.freeeed.lotus.NSFXDataParser;
import org.freeeed.mail.EmailDataProvider;
import org.freeeed.mail.EmlParser;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is separate to have all Tika-related stuff in a one place It may contain more parsing specifics later on
 */
public class DocumentParser {

    private static final Logger logger = LoggerFactory.getLogger(DocumentParser.class);
    private static DocumentParser instance = new DocumentParser();
    private Tika tika;

    public static DocumentParser getInstance() {
        return instance;
    }

    private DocumentParser() {
        tika = new Tika();
        tika.setMaxStringLength(10 * 1024 * 1024);
    }

    public void parse(DiscoveryFile discoveryFile, DocumentMetadata metadata) {
        logger.debug("Parsing file: {}, original file name: {}", discoveryFile.getPath().getPath(),
                discoveryFile.getRealFileName());

        TikaInputStream inputStream = null;
        try {
            String extension = Util.getExtension(discoveryFile.getRealFileName());
            logger.debug("Detected extension: {}", extension);

            if ("eml".equalsIgnoreCase(extension)) {
                EmlParser emlParser = new EmlParser(discoveryFile.getPath());
                extractEmlFields(discoveryFile.getPath().getPath(), metadata, emlParser);

                inputStream = TikaInputStream.get(discoveryFile.getPath());
                String text = tika.parseToString(inputStream, metadata);
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);

                parseDateTimeReceivedFields(metadata);
                parseDateTimeSentFields(metadata, emlParser.getSentDate());
            } else if ("nsfe".equalsIgnoreCase(extension)) {
                NSFXDataParser emlParser = new NSFXDataParser(discoveryFile.getPath());
                extractEmlFields(discoveryFile.getPath().getPath(), metadata, emlParser);
            } else {
                // the given input stream is closed by the parseToString method (see Tika documentation)
                // we will close it just in case :)            
                inputStream = TikaInputStream.get(discoveryFile.getPath());
                String text = tika.parseToString(inputStream, metadata);
                metadata.setDocumentText(text);
            }

        } catch (Exception e) {
            // the show must still go on
            logger.info("Exception: " + e.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());

            logger.error("Problem parsing file", e);
        } catch (OutOfMemoryError m) {
            logger.error("Out of memory, trying to continue", m);
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

                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
                dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String dateOnly = dateFormatter.format(dateObj);

                metadata.setMessageDate(dateOnly);
                metadata.setMessageDateReceived(dateOnly);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeOnly = timeFormatter.format(dateObj);

                metadata.setMessageTimeReceived(timeOnly);
            } catch (Exception e) {
                logger.error("Problem extracting date time fields" + e.toString());
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

            if (emlParser.getTo() != null) {
                metadata.setMessageTo(getAddressLine(emlParser.getTo()));
            }

            if (emlParser.getCC() != null) {
                metadata.setMessageCC(getAddressLine(emlParser.getCC()));
            }

            if (emlParser.getDate() != null) {
                metadata.setMessageCreationDate(formatDate(emlParser.getDate()));
            }

        } catch (Exception e) {
            logger.error("Problem parsing eml file " + e.toString());
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
