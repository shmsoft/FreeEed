package org.freeeed.mail;

import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
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

        // Assuming you are sending email from localhost
        String host = "mail.top8.biz";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        properties.setProperty("mail.user", "freeeed+top8.biz");
        properties.setProperty("mail.password", "freeeed123");
        
        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

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
            System.out.println("Sent message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace(System.out);
            return false;
        }
        return true;
    }
}
