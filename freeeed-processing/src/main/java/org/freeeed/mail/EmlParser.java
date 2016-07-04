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
package org.freeeed.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmlParser implements EmailDataProvider {

    private static final Logger log = LoggerFactory.getLogger(EmlParser.class);

    private File emailFile;
    private ArrayList<String> to;
    private Address[] _bcc;
    private Address[] _cc;
    private Address[] _to;
    private Address[] _from;
    private String _subject;
    private Object _content;
    private MimeMessage email;
    private final List<String> _attachments;
    private Date _date;
    private Date _sentDate;
    private final Map<String, String> attachmentsContent;
    private int attachmentSeq = 0;

    public EmlParser(File emailFile) throws Exception {
        this.emailFile = emailFile;
        _attachments = new ArrayList<>();
        System.setProperty("mail.mime.address.strict", "false");
        System.setProperty("mail.mime.decodeparameters", "true");
        attachmentsContent = new HashMap<>();

        parseEmail();
    }

    private void parseEmail() throws Exception {
        java.util.Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(emailFile);
            email = new MimeMessage(session, fis);
            _bcc = email.getRecipients(RecipientType.BCC);
            _cc = email.getRecipients(RecipientType.CC);
            _to = email.getRecipients(RecipientType.TO);
            _from = email.getFrom();
            _subject = email.getSubject();
            _content = email.getContent();
            _date = email.getReceivedDate();
            _sentDate = email.getSentDate();
            //System.out.println("content type: " + email.getContentType());
            //System.out.println("\nsubject: " + email.getSubject());
            //to = EmailUtil.parseAddressLines(email
            //       .getHeader(Message.RecipientType.TO.toString()));
        } catch (MessagingException e) {
            throw new IllegalStateException("illegal state issue", e);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("file not found issue issue: "
                    + emailFile.getAbsolutePath(), e);
        } catch (IOException e) {
            log.error("Problem parsing eml file", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.warn("Problem closing the stream", e);
                }
            }
        }
    }

    private List<String> getAddressAsList(Address[] address) {
        List<String> result = new ArrayList<>();
        if (address != null) {
            for (Address a : address) {
                result.add(a.toString());
            }
        }
        return result;
    }

    @Override
    public List<String> getFrom() {
        return getAddressAsList(_from);
    }

    @Override
    public List<String> getRecepient() {
        return getAddressAsList(_to);
    }

    @Override
    public List<String> getCC() {
        return getAddressAsList(_cc);
    }

    @Override
    public List<String> getBCC() {
        return getAddressAsList(_bcc);
    }

    @Override
    public String getSubject() {
        return _subject;
    }

    @Override
    public Date getDate() {
        return _date;
    }

    @Override
    public String getContent() throws MessagingException, IOException {
        if (_content instanceof String) {
            return _content.toString();
        }

        if (_content instanceof MimeMultipart) {
            MimeMultipart cnt = (MimeMultipart) _content;
            int size = cnt.getCount();
            StringBuilder res = new StringBuilder();

            for (int i = 0; i < size; ++i) {
                BodyPart bp = cnt.getBodyPart(i);
                res.append(dumpPart(bp));
            }
            return res.toString();
        }

        return "";
    }

    private String dumpPart(Part p) throws MessagingException, IOException {

        StringBuilder buf = new StringBuilder();
        if (p.isMimeType("text/plain")) {
            buf.append(p.getContent());
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    buf.append(dumpPart(bp));
                }
            }
        } else if (p.isMimeType("message/rfc822")) {
            buf.append(dumpPart((Part) p.getContent()));
        } else {
            String disp = null;
            try {
                disp = p.getDisposition();
            } catch (Exception e) {
            }

            String filename = "attach-" + (attachmentSeq++);
            try {
                filename = p.getFileName();
            } catch (Exception e) {
                log.error("Problem getting the real attachment name", e);
            }

            if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
                log.debug("Adding attachment: " + filename);

                _attachments.add(filename);

                if (Project.getCurrentProject().isAddEmailAttachmentToPDF()) {
                    log.debug("Parsing the attachment content with Tika");

                    Tika tika = new Tika();
                    tika.setMaxStringLength(10 * 1024 * 1024);
                    try {
                        String attachmentContent = tika.parseToString(p.getInputStream(), new Metadata());
                        attachmentsContent.put(filename, attachmentContent);

                        log.debug("Attachment content parsed!");
                    } catch (TikaException e) {
                        log.error("Problem parsing attachment", e);
                    }
                }
            }
        }
        return buf.toString();
    }

    public static void main(String argv[]) throws Exception {
        EmlParser instance = new EmlParser(new File("samples/13.eml"));
        System.out.println(instance.getContent());
        System.out.println(instance.getAttachmentNames());
        //instance.parseEmail();
        /*
         * ArrayList<String> to = instance.getTo(); for (String t : to) {
         * System.out.println(t); } instance = new EmlParser(new
         * File("test-data/jpst/802.eml")); try { instance.saveAttachments(); }
         * catch (Exception e) { e.printStackTrace(System.out); }
         */
    }

    /**
     * @return the emailFile
     */
    public File getEmailFile() {
        return emailFile;
    }

    /**
     * @param emailFile the emailFile to set
     */
    public void setEmailFile(File emailFile) {
        this.emailFile = emailFile;
    }

    /**
     * @param to the to to set
     */
    public void setTo(ArrayList<String> to) {
        this.to = to;
    }

    /**
     * @return the to
     */
    @Override
    public List<String> getTo() {
        return getAddressAsList(_to);
    }

    @Override
    public List<String> getAttachmentNames() {
        return _attachments;
    }

    @Override
    public Date getSentDate() {
        return this._sentDate;
    }

    public void saveAttachments() throws MessagingException, IOException {
        if (email.isMimeType("text/*")) {
            // no attachments there - this is just the email itself
        }
        // TODO - why repeat the code?
        if (email.isMimeType("multipart/alternative")) {
            Multipart mp = (Multipart) email.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                MimeBodyPart bodyPart = (MimeBodyPart) mp.getBodyPart(i);
                String attachmentFileName = bodyPart.getFileName();
                if (attachmentFileName != null) {
                    bodyPart.saveFile(attachmentFileName);
                }
            }
        } else if (email.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) email.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                MimeBodyPart bodyPart = (MimeBodyPart) mp.getBodyPart(i);
                String attachmentFileName = bodyPart.getFileName();
                if (attachmentFileName != null) {
                    bodyPart.saveFile(attachmentFileName);
                }
            }
        }
    }

    @Override
    public Map<String, String> getAttachmentsContent() {
        return attachmentsContent;
    }
}
