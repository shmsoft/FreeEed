package org.freeeed.mail;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

/**
 * Not used right now
 * This will work for MBOX, but one still needs to do one's own parsing 
 * of TO, CC, BCC fields, so we will do it later
 * @author mark
 */
public class Mstor {

    public static void main(String[] args) {
        Session session = Session.getDefaultInstance(new Properties());
        try {
            Store store = session.getStore(new URLName("mstor:test-data/readpst/Sent"));            
            store.connect();
            // read messages from Inbox..
            Folder inbox = store.getDefaultFolder();                        
            inbox.open(Folder.READ_ONLY);
            Folder [] folders = inbox.list();
            System.out.println("folder count: " + folders.length);
            Message[] messages = inbox.getMessages();
            System.out.println("message count: " + messages.length);
            try {
                for (int i = 0, n = messages.length; i < n; i++) {
                    System.out.println(i + ": " + messages[i].getFrom()[0]
                            + "\t" + messages[i].getSubject());
                    messages[i].writeTo(System.out);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(System.out);
            }
            inbox.close(false); // expunges all deleted messages if this flag is true
            store.close();
        } catch (MessagingException me) {
            me.printStackTrace(System.out);
        }
    }
}