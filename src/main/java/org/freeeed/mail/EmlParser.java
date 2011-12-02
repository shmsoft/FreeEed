package org.freeeed.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class EmlParser {
    private File emailFile;
    private ArrayList <String> to;
    
    public EmlParser(File emailFile) {
        this.emailFile = emailFile;
        parseEmail();
    }
    private void parseEmail() {
        java.util.Properties properties = System.getProperties();
        Session session = Session.getDefaultInstance(properties);
        MimeMessage email = null;
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
        EmlParser instance = new EmlParser(new File("test-data/jpst/2147"));
        ArrayList <String> to = instance.getTo();
        for (String t: to) {
            System.out.println(t);
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
    public void setTo(ArrayList <String> to) {
        this.to = to;
    }

    /**
     * @return the to
     */
    public ArrayList <String> getTo() {
        return to;
    }
}
