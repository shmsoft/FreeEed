package org.freeeed.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class EmlParser {

    public void parseDir(String dir) {
        File[] mailFiles = new File(dir).listFiles();
        String host = "host.com";
        java.util.Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);
        for (File tmpFile : mailFiles) {
            MimeMessage email = null;
            try {
                FileInputStream fis = new FileInputStream(tmpFile);
                email = new MimeMessage(session, fis);
                System.out.println("content type: " + email.getContentType());
                System.out.println("\nsubject: " + email.getSubject());
                String[] addressLine = email.getHeader(Message.RecipientType.TO.toString());
                String[] addresses = addressLine[0].split(",");
                for (String address : addresses) {
                    System.out.println(address.trim().replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", ""));
                }
                //System.out.println("\nrecipients: " + Arrays.asList(email.getRecipients(Message.RecipientType.TO)));
            } catch (MessagingException e) {
                throw new IllegalStateException("illegal state issue", e);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("file not found issue issue: " + tmpFile.getAbsolutePath(), e);
            }
        }
    }

    public static void main(String argv[]) {
        EmlParser instance = new EmlParser();
        instance.parseDir("test-data/jpst");
    }
}
