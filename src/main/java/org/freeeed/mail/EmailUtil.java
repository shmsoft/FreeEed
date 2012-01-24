package org.freeeed.mail;

import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author mark
 */
public class EmailUtil {

    public static ArrayList<String> parseAddressLines(String[] addressLines) {
        ArrayList<String> fields = new ArrayList<String>();
        for (String addressLine : addressLines) {
            String[] addresses = addressLine.split(",");
            for (String address : addresses) {
                address = address.trim().replaceAll("\t", "").replaceAll("\n", "").replaceAll("\r", "");
                fields.add(address);
            }
        }
        return fields;
    }

    public static boolean sendEmail(String text) {
        String to = "freeeed@shmsoft.com";

        String from = "freeeed@top8.biz";

        String host = "mail.top8.biz";

        Properties properties = System.getProperties();

        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.user", "freeeed+top8.biz");
        properties.setProperty("mail.password", "freeeed123");

        properties.put("mail.smtp.socketFactory.port", "465");
        properties.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("freeeed+top8.biz", "freeeed123");
                    }
                });        

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);


            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Improvement suggestion for FreeEed");

            // Now set the actual message
            message.setText(text);

            // Send message            
            Transport.send(message);            
        } catch (MessagingException mex) {
            mex.printStackTrace(System.out);
            return false;
        }
        return true;
    }
}
