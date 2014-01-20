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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.tika.io.IOUtils;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Project;

import com.google.common.base.Charsets;

/**
 *
 * @author mark
 */
public class EmailUtil {
    private static int bate = 0;
    
    private static String HTML_TEMPLATE;
    static {
        try {
            HTML_TEMPLATE = new String(IOUtils.toByteArray(
                    EmailUtil.class.getClassLoader().getResourceAsStream(ParameterProcessing.EML_HTML_TEMPLATE_FILE)),
                    Charsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Unable to load html template - " + e.getMessage());
        }
    }
    
    private static String HTML_TEMPLATE_NOCDATA;
    static {
        try {
            HTML_TEMPLATE_NOCDATA = new String(IOUtils.toByteArray(
                    EmailUtil.class.getClassLoader().getResourceAsStream(ParameterProcessing.EML_HTML_TEMPLATE_FILE_NO_CDATA)),
                    Charsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Unable to load html template - " + e.getMessage());
        }
    }
    
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

    /**
     * Parse the given eml file, extract it fields.
     * 
     * Using the html template, defined in assets dir,
     * substitute the placeholder there with the real eml fields.
     * 
     * Return the constructed html for further usage.
     * 
     * @param emlFile
     * @return
     */
    public static String createHtmlFromEmlFile(String emlFile, EmailDataProvider emlParser) throws IOException {
        String html = HTML_TEMPLATE;
        return createHtmlFromEmlFileImp(html, emlFile, emlParser, true);
    }
    
    public static String createHtmlFromEmlFileNoCData(String emlFile, EmailDataProvider emlParser) throws IOException {
        String html = HTML_TEMPLATE_NOCDATA;
        return createHtmlFromEmlFileImp(html, emlFile, emlParser, false);
    }
    
    private static String createHtmlFromEmlFileImp(String html, String emlFile, EmailDataProvider emlParser, boolean cdata) throws IOException {
        html = html.replaceAll("@BATE@", Project.getProject().getProjectName() + "-" + Project.getProject().getProjectCode() + " " + (++bate));
                
        html = html.replaceAll("@FROM@", "" + Matcher.quoteReplacement(getAddressLine(emlParser.getFrom())));
        html = html.replaceAll("@TO@", "" + Matcher.quoteReplacement(getAddressLine(emlParser.getRecepient())));
        html = html.replaceAll("@CC@", "" + Matcher.quoteReplacement(getAddressLine(emlParser.getCC())));
        html = html.replaceAll("@BCC@", "" + Matcher.quoteReplacement(getAddressLine(emlParser.getBCC())));
        html = html.replaceAll("@SUBJECT@", "" + Matcher.quoteReplacement(emlParser.getSubject()));
        
        String dateStr = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (emlParser.getSentDate() != null) {
            dateStr = sdf.format(emlParser.getSentDate());
        } else if (emlParser.getDate() != null) {
            dateStr = sdf.format(emlParser.getDate());
        }
        
        html = html.replaceAll("@DATE@", dateStr);
        
        try {
            String bodyContent = prepareContent(emlParser.getContent(), cdata);
            String bodyEsc = Matcher.quoteReplacement(bodyContent);
            
            html = html.replaceAll("@BODY@", " " + bodyEsc);
            
            String attachments = Matcher.quoteReplacement(getAttachments(
                    emlParser.getAttachmentNames(), emlParser.getAttachmentsContent()));
            html = html.replaceAll("@ATTACH@", " " + attachments);
        } catch (MessagingException e) {
            throw new IOException(e);
        }
        
        return html;
    }
    
    private static String prepareContent(String content, boolean cdata) {
        StringBuffer result = new StringBuffer();
        
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (cdata) {
                result.append("<![CDATA[");
            }
            
            result.append(line);
            
            if (cdata) {
                result.append("]]>");
            }
            result.append("<br/>");
        }
        
        return result.toString();
    }
    
    private static String getAddressLine(List<String> addresses) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < addresses.size(); i++) {
            String address = addresses.get(i);
            result.append(address);
            if (i < addresses.size() - 1) {
                result.append(" , ");
            }
        }
        
        return result.toString();
    }
    
    private static String getAttachments(List<String> attachments, Map<String, String> attachmentData) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < attachments.size(); i++) {
            String attach = attachments.get(i);
            result.append("<![CDATA[");
            result.append(attach);
            result.append("]]>");
            result.append("<br/>");

            String content = attachmentData != null ? attachmentData.get(attach) : null;
            if (content != null) {
                result.append("--------------------------------------------------");
                result.append("<br/>");
                result.append("<![CDATA[");
                result.append(content);
                result.append("]]>");
                result.append("<br/>");
                result.append("--------------------------------------------------");
                result.append("<br/>");
                result.append("<br/>");
            }
        }
        
        return result.toString();
    }
    
    public static boolean sendEmail(String subject, String messageText) {
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
            message.setSubject(subject);

            // Now set the actual message
            message.setText(messageText);

            // Send message            
            Transport.send(message);            
        } catch (MessagingException mex) {
            mex.printStackTrace(System.out);
            return false;
        }
        return true;
    }
}
