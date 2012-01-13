package org.freeeed.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

public class EmlParser {

    private File emailFile;
    private ArrayList<String> to;
    private MimeMessage email;

    public EmlParser(File emailFile) {
        this.emailFile = emailFile;
        parseEmail();
    }

    private void parseEmail() {
        java.util.Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);
        try {
            FileInputStream fis = new FileInputStream(emailFile);
            email = new MimeMessage(session, fis);
            System.out.println("content type: " + email.getContentType());
            System.out.println("\nsubject: " + email.getSubject());
            to = EmailUtil.parseAddressLines(email.getHeader(Message.RecipientType.TO.toString()));
        } catch (MessagingException e) {
            throw new IllegalStateException("illegal state issue", e);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("file not found issue issue: " + emailFile.getAbsolutePath(), e);
        }
    }

    public static void main(String argv[]) {
        EmlParser instance = new EmlParser(new File("test-data/jpst/2147.eml"));
        ArrayList<String> to = instance.getTo();
        for (String t : to) {
            System.out.println(t);
        }
        instance = new EmlParser(new File("test-data/jpst/802.eml"));
        try {
            instance.saveAttachments();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
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
    public ArrayList<String> getTo() {
        return to;
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
}