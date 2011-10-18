/**
 * This is a proprietary driver for PST processing
 * It comes from IndependentSoft, and has its own license
 */
package org.freeeed.thirdparty;
    
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.util.ByteArrayDataSource;

import com.independentsoft.pst.Folder;
import com.independentsoft.pst.Item;
import com.independentsoft.msg.Attachment;
import com.independentsoft.msg.Message;
import com.independentsoft.msg.Recipient;
import com.independentsoft.pst.PstFile;

/**
 *
 * @author mark
 */
public class ExportEmlJpst {
    private Session session;
    private int emlCounter;
    
    public static void main(String argv[]) {
        String pathPath = argv[0];
        String outputFolder = argv[1];
        ExportEmlJpst instance = new ExportEmlJpst();
        instance.extractEml(pathPath, outputFolder);
    }
    public void extractEml(String pstPath, String outputFolder) {
        String parentFolderPath = outputFolder;
        Properties properties = System.getProperties();        
        session = Session.getInstance(properties); 
        emlCounter = 1;
        
        try {
            PstFile file = new PstFile(pstPath);

            try {
                List<Folder> allFolders = file.getRoot().getFolders(true);

                HashMap<Long, String> parents = new HashMap<Long, String>();
   
                File newFolder = new File(outputFolder);
                newFolder.mkdirs();

                parents.put(file.getRoot().getId(), parentFolderPath);

                for (int p = 0; p < allFolders.size(); p++) {
                    Folder currentFolder = allFolders.get(p);

                    parentFolderPath = parents.get(currentFolder.getParentId());

                    newFolder = new File(parentFolderPath + File.separator + currentFolder.getDisplayName());
                    newFolder.mkdirs();

                    parents.put(currentFolder.getId(), parentFolderPath + File.separator + currentFolder.getDisplayName());
                }

                for (int j = 0; j < allFolders.size(); j++) {
                    for (int s = 0; s < allFolders.get(j).getChildrenCount(); s += 100) {
                        List<Item> items = allFolders.get(j).getItems(s, s + 100);

                        for (int k = 0; k < items.size(); k++) {
                            Message message = items.get(k).getMessageFile();

                            MimeMessage mimeMessage = convertToMimeMessage(message);

                            String parentFolder = parents.get(items.get(k).getParentId());
                            //String fileName = getFileName(items.get(k).getSubject());
                            String fileName = Integer.toString(emlCounter);
                            ++emlCounter;
                            
                            String filePath = parentFolder + File.separator + fileName;

                            if (filePath.length() > 244) {
                                filePath = filePath.substring(0, 244);
                            }

                            //filePath = filePath + "-" + items.get(k).getId() + ".eml";                            

                            File emlFile = new File(filePath);
                            mimeMessage.writeTo(new FileOutputStream(emlFile));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            } catch (MessagingException e) {
                e.printStackTrace(System.out);
            } finally {
                if (file != null) {
                    file.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private MimeMessage convertToMimeMessage(Message message) throws MessagingException, IOException {                       
        MimeMessage mimeMessage = new MimeMessage(session);

        if (message.getTransportMessageHeaders() != null) {
            InternetHeaders headers = new InternetHeaders(new ByteArrayInputStream(message.getTransportMessageHeaders().getBytes()));
            headers.removeHeader("Content-Type");

            Enumeration<Header> allHeaders = headers.getAllHeaders();

            while (allHeaders.hasMoreElements()) {
                Header header = allHeaders.nextElement();
                mimeMessage.addHeader(header.getName(), header.getValue());
            }
        } else {
            mimeMessage.setSubject(message.getSubject());
            mimeMessage.setSentDate(message.getClientSubmitTime());

            InternetAddress fromMailbox = new InternetAddress();

            fromMailbox.setAddress(message.getSenderEmailAddress());

            if (message.getSenderName() != null && message.getSenderName().length() > 0) {
                fromMailbox.setPersonal(message.getSenderName());
            } else {
                fromMailbox.setPersonal(message.getSenderEmailAddress());
            }

            mimeMessage.setFrom(fromMailbox);

            for (int i = 0; i < message.getRecipients().size(); i++) {
                Recipient recipient = message.getRecipients().get(i);

                if (recipient.getRecipientType() == com.independentsoft.msg.RecipientType.TO) {
                    mimeMessage.setRecipient(RecipientType.TO, new InternetAddress(recipient.getEmailAddress(), recipient.getDisplayName()));
                } else if (recipient.getRecipientType() == com.independentsoft.msg.RecipientType.CC) {
                    mimeMessage.setRecipient(RecipientType.CC, new InternetAddress(recipient.getEmailAddress(), recipient.getDisplayName()));
                } else if (recipient.getRecipientType() == com.independentsoft.msg.RecipientType.BCC) {
                    mimeMessage.setRecipient(RecipientType.BCC, new InternetAddress(recipient.getEmailAddress(), recipient.getDisplayName()));
                }
            }
        }

        MimeMultipart rootMultipart = new MimeMultipart();
        MimeMultipart contentMultipart = new MimeMultipart();

        MimeBodyPart contentBodyPart = new MimeBodyPart();
        contentBodyPart.setContent(contentMultipart);

        if (message.getBody() != null && message.getBody().length() > 0) {
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(message.getBody());
            contentMultipart.addBodyPart(textBodyPart);
        }

        if (message.getBodyHtmlText() != null) {
            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            String htmlPart = message.getBodyHtmlText();
            htmlBodyPart.setDataHandler(new DataHandler(new ByteArrayDataSource(htmlPart, "text/html")));
            contentMultipart.addBodyPart(htmlBodyPart);
        }

        if (message.getBody() == null && message.getBodyHtmlText() == null) {
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText("<<Empty Body>>");
            textBodyPart.addHeaderLine("Content-Type: text/plain; charset=\"utf-8\"");
            textBodyPart.addHeaderLine("Content-Transfer-Encoding: quoted-printable");
            contentMultipart.addBodyPart(textBodyPart);
        }

        rootMultipart.addBodyPart(contentBodyPart);

        for (int i = 0; i < message.getAttachments().size(); i++) {
            Attachment attachment = message.getAttachments().get(i);

            if (attachment != null && attachment.toByteArray() != null) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();

                if (attachment.getMimeTag() != null) {
                    DataSource source = new ByteArrayDataSource(attachment.toByteArray(), attachment.getMimeTag());
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                } else {
                    DataSource source = new ByteArrayDataSource(attachment.toByteArray(), "application/octet-stream");
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                }

                attachmentBodyPart.setContentID(attachment.getContentId());

                String fileName = "";

                if (attachment.getLongFileName() != null) {
                    fileName = attachment.getLongFileName();
                } else if (attachment.getDisplayName() != null) {
                    fileName = attachment.getDisplayName();
                } else if (attachment.getFileName() != null) {
                    fileName = attachment.getFileName();
                }

                attachmentBodyPart.setFileName(fileName);

                rootMultipart.addBodyPart(attachmentBodyPart);
            }
        }

        mimeMessage.setContent(rootMultipart);

        return mimeMessage;
    }

//    private String getFileName(String subject) {
//        if (subject == null || subject.length() == 0) {
//            String fileName = "NoSubject";
//            return fileName;
//        } else {
//            String fileName = "";
//
//            for (int i = 0; i < subject.length(); i++) {
//                if (subject.charAt(i) > 31 && subject.charAt(i) < 127) {
//                    fileName += subject.charAt(i);
//                }
//            }
//
//            fileName = fileName.replace("\\", "_");
//            fileName = fileName.replace("/", "_");
//            fileName = fileName.replace(":", "_");
//            fileName = fileName.replace("*", "_");
//            fileName = fileName.replace("?", "_");
//            fileName = fileName.replace("\"", "_");
//            fileName = fileName.replace("<", "_");
//            fileName = fileName.replace(">", "_");
//            fileName = fileName.replace("|", "_");
//
//            return fileName;
//        }
//    }
}
