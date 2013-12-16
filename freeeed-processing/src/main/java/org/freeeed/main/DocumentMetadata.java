package org.freeeed.main;

import org.apache.tika.metadata.Metadata;

/**
 *
 * @author mark Class to hold specific document metadata of interest in discovery.
 * For field definition it uses a number of keys defined in Tika metadata interfaces, as well as some custom keys.
 *
 */
public class DocumentMetadata extends Metadata {

    private static final String DOCUMENT_ORIGINAL_PATH = "document_original_path";
    private static final String DOCUMENT_TEXT = "text";
    private static final String PROCESSING_EXCEPTION = "processing_exception";
    private static final String MASTER_DUPLICATE = "master_duplicate";
    private static final String CUSTODIAN = "Custodian";
    private static final String LINK_NATIVE = "native_link";
    private static final String LINK_TEXT = "text_link";
    private static final String LINK_EXCEPTION = "exception_link";

    // TODO the following group of fields hides fields inherited from interfaces. Decide what to do with it.
    public static final String SUBJECT = "subject";
    public static final String MESSAGE_FROM = "Message-From";
    public static final String MESSAGE_CREATION_DATE = "Creation-Date";
    public static final String MESSAGE_TO = "Message-To";
    public static final String MESSAGE_CC = "Message-Cc";
    
    public static final String DATE = "date";
    public static final String DATE_RECEIVED = "Date Received";
    public static final String TIME_RECEIVED = "Time Received";
    
    public static final String DATE_SENT = "Date Sent";
    public static final String TIME_SENT = "Time Sent";
    
    public String getOriginalPath() {
        return get(DOCUMENT_ORIGINAL_PATH);
    }
    public void setOriginalPath(String originalPath) {
        set(DOCUMENT_ORIGINAL_PATH, originalPath);
    }
    public String getDocumentText() {
        return get(DOCUMENT_TEXT);
    }
    public void setDocumentText(String documentText) {
        set(DOCUMENT_TEXT, documentText);
    }
    
    public String getMessageSubject() {
        return get(SUBJECT);
    }
    
    public void setMessageSubject(String subject) {
        set(SUBJECT, subject);
    }
    
    public String getMessageFrom() {
        return get(MESSAGE_FROM);
    }
    
    public void setMessageFrom(String messageFrom) {
       set(MESSAGE_FROM, messageFrom);
    }
    
    public String getMessageCreationDate() {
        return get(MESSAGE_CREATION_DATE);
    }
    
    public void setMessageCreationDate(String messageCreationDate) {
        set(MESSAGE_CREATION_DATE, messageCreationDate);
    }
    
    public String getMessageTo() {
        return get(MESSAGE_TO);
    }
    
    public void setMessageTo(String messageTo) {
        set(MESSAGE_TO, messageTo);
    }
    
    public String getMessageCC() {
        return get(MESSAGE_CC);
    }
    
    public void setMessageCC(String messageCC) {
        set(MESSAGE_CC, messageCC);
    }
    
    public String getMessageDate() {
        return get(DATE);
    }
    
    public void setMessageDate(String date) {
        set(DATE, date);
    }
    
    public String getMessageDateReceived() {
        return get(DATE_RECEIVED);
    }
    
    public void setMessageDateReceived(String m) {
        set(DATE_RECEIVED, m);
    }
    
    public String getMessageTimeReceived() {
        return get(TIME_RECEIVED);
    }
    
    public void setMessageTimeReceived(String s) {
        set(TIME_RECEIVED, s);
    }
    
    public String getMessageDateSent() {
        return get(DATE_SENT);
    }
    
    public void setMessageDateSent(String s) {
        set(DATE_SENT, s);
    }
    
    public String getMessageTimeSent() {
        return get(TIME_SENT);
    }
    
    public void setMessageTimeSent(String s) {
        set(TIME_SENT, s);
    }
    /**
     * Similar to super.add(), but with an additional return type, for fluent interface pattern.
     * @param key key in the hashmap to be added.
     * @param value value in the hashmap to be added.
     * @return 
     */
    public DocumentMetadata addField(String key, String value) {
        this.add(key, value);
        return this;
    }
}
