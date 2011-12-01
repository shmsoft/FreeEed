package org.freeeed.mail;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;


public class Mstor {

    public static void main(String[] args) {
        Session session = Session.getDefaultInstance(new Properties());
        try {
            Store store = session.getStore(new URLName("mstor:test-data/readpst/Sent"));            
            //Store store = session.getStore(new URLName("mstor:test-data/readpst/18"));            
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
                ioe.printStackTrace();
            }
            inbox.close(false); // expunges all deleted messages if this flag is true
            store.close();
        } catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}