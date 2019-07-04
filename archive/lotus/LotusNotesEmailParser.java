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
package org.freeeed.lotus;


import lotus.domino.*;
import org.freeeed.data.index.ESIndex;
import org.freeeed.data.index.ESIndexUtil;
import org.freeeed.mail.EmailUtil;
import org.freeeed.main.DocumentMetadata;
import org.freeeed.main.DocumentParser;
import org.freeeed.services.Project;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class LotusNotesEmailParser.
 *
 * @author ilazarov.
 */
public class LotusNotesEmailParser {
    private static final SimpleDateFormat df1 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private FileStorage fileStorage;
    private String lotusDominoEmailDBFile;

    public LotusNotesEmailParser(String nsfFile, String outputDir, String esEndpoint) {
        fileStorage = new FileStorage(outputDir, nsfFile);
        lotusDominoEmailDBFile = nsfFile;
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

        @SuppressWarnings({"unchecked", "rawtypes"})
        public void runNotes() {
            try {
                Session s = NotesFactory.createSession();
                Database db = s.getDatabase("", nsfFile);
                View vw = db.getView("By Person");

                Document doc = vw.getFirstDocument();
                while (doc != null) {
                    LotusEmail le = new LotusEmail();

                    le.setSubject(doc.getItemValueString("Subject"));

                    List<String> from = new ArrayList<>();
                    from.addAll(doc.getItemValue("From"));
                    le.setFrom(from);

                    List<String> to = new ArrayList<>();
                    to.addAll(doc.getItemValue("SendTo"));
                    le.setTo(to);

                    List<String> cc = new ArrayList<>();
                    cc.addAll(doc.getItemValue("CopyTo"));
                    le.setCc(cc);

                    List<String> bcc = new ArrayList<>();
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
                    } catch (Exception ignored) {
                    }

                    RichTextItem body = (RichTextItem) doc.getFirstItem("Body");
                    le.setContent(body.getText());

                    List<String> attachments = new ArrayList<>();

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
                    sendToES(le);

                    doc = vw.getNextDocument(doc);
                }

                success = true;
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println("ERROR LotusNotesEmailParser -- fail NSF parsing: " + e.getMessage());
                caughtException = e;
                success = false;
            }
        }
    }

    private void sendToES(LotusEmail email) {
        if (Project.getCurrentProject().isSendIndexToESEnabled()) {
            Map<String, Object> metadata = new HashMap<>();

            String text = prepareContent(email.getContent());
            List<String> attachments = email.getAttachmentNames();
            text = DocumentParser.parseAttachment(text, attachments);

            metadata.put("text", text);
            if (email.getFrom() != null) {
                metadata.put("Message-From", EmailUtil.getAddressLine(email.getFrom()));
            }

            if (email.getSubject() != null) {
                metadata.put("subject", email.getSubject());
            }

            if (email.getTo() != null) {
                metadata.put("Message-To", EmailUtil.getAddressLine(email.getTo()));
            }

            if (email.getCC() != null) {
                metadata.put("Message-Cc", EmailUtil.getAddressLine(email.getCC()));
            }

            if (email.getDate() != null) {
                metadata.put("Creation-Date", formatDate(email.getDate()));
            }

            String projectCode = Project.getCurrentProject().getProjectCode();
            ESIndexUtil.addDocToES(metadata, ESIndex.ES_INSTANCE_DIR + "_" + projectCode, (String) metadata.get(DocumentMetadata.UNIQUE_ID));
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
}
