package org.freeeed.main;

import org.apache.tika.metadata.Metadata;
import org.freeeed.services.DuplicatesTracker;
import org.freeeed.services.UniqueIdGenerator;

/**
 * @author mark Class to hold specific document metadata of interest in
 * discovery. For field definition it uses a number of keys defined in Tika
 * metadata interfaces, as well as some custom keys.
 */
public class DocumentMetadata extends Metadata {

    private static final String DOCUMENT_ORIGINAL_PATH = "document_original_path";
    private static final String DOCUMENT_PARENT = "document_parent";
    private static final String DOCUMENT_TEXT = "text";
    private static final String HAS_ATTACHMENTS = "has_attachments";
    private static final String HAS_PARENT = "has_parent";
    private static final String HASH = "Hash";
    //    private static final String PROCESSING_EXCEPTION = "processing_exception";
    private static final String MASTER_DUPLICATE = "master_duplicate";
    private static final String CUSTODIAN = "Custodian";
    private static final String LINK_NATIVE = "native_link";
    private static final String TEXT_LINK = "text_link";
    private static final String LINK_EXCEPTION = "exception_link";

    // TODO the following group of fields hides fields inherited from interfaces. Decide what to do with it.
    private static final String SUBJECT = "subject";
    private static final String MESSAGE_FROM = "Message-From";
    private static final String MESSAGE_CREATION_DATE = "Creation-Date";
    private static final String MESSAGE_TO = "Message-To";
    private static final String MESSAGE_CC = "Message-Cc";
    private static final String DATE = "date";
    private static final String DATE_RECEIVED = "Date Received";
    private static final String TIME_RECEIVED = "Time Received";
    private static final String DATE_SENT = "Date Sent";
    private static final String TIME_SENT = "Time Sent";
    public static final String UNIQUE_ID = "UPI";
    private static final String MESSAGE_ID = "message_id";
    private static final String REFERENCES = "references";
    private static final String FILETYPE = "File Type";

    public String getOriginalPath() {
        return get(DOCUMENT_ORIGINAL_PATH);
    }

    void setOriginalPath(String originalPath) {
        set(DOCUMENT_ORIGINAL_PATH, originalPath);
    }

    public String getDocumentParent() {
        return get(DOCUMENT_PARENT);
    }

    public String getHash() {
        return get(HASH);
    }

    public void setDocumentParent(String parentPath) {
        set(DOCUMENT_PARENT, parentPath);
    }

    public String getCustodian() {
        return get(CUSTODIAN);
    }

    public void setCustodian(String custodian) {
        set(CUSTODIAN, custodian);
    }

    public String getDocumentText() {
        return get(DOCUMENT_TEXT);
    }

    void setDocumentText(String documentText) {
        set(DOCUMENT_TEXT, documentText);
    }

    public String getMessageSubject() {
        return get(SUBJECT);
    }

    void setMessageSubject(String subject) {
        set(SUBJECT, subject);
    }

    void setFiletype(String filetype) {
        set(FILETYPE, filetype);
    }

    public void setHash(String hash) {
        set(HASH, hash);
    }

    public String getFiletype() {
        return get(FILETYPE);
    }

    public String getMessageFrom() {
        return get(MESSAGE_FROM);
    }

    void setMessageFrom(String messageFrom) {
        set(MESSAGE_FROM, messageFrom);
    }

    public String getMessageCreationDate() {
        return get(MESSAGE_CREATION_DATE);
    }

    void setMessageCreationDate(String messageCreationDate) {
        set(MESSAGE_CREATION_DATE, messageCreationDate);
    }

    public String getMessageTo() {
        return get(MESSAGE_TO);
    }

    void setMessageTo(String messageTo) {
        set(MESSAGE_TO, messageTo);
    }

    public String getMessageCC() {
        return get(MESSAGE_CC);
    }

    void setMessageCC(String messageCC) {
        set(MESSAGE_CC, messageCC);
    }

    String getMessageDate() {
        return get(DATE);
    }

    void setMessageDate(String date) {
        set(DATE, date);
    }

    public String getMessageDateReceived() {
        return get(DATE_RECEIVED);
    }

    void setMessageDateReceived(String m) {
        set(DATE_RECEIVED, m);
    }

    public String getMessageTimeReceived() {
        return get(TIME_RECEIVED);
    }

    void setMessageTimeReceived(String s) {
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

    String getContentType() {
        return get(CONTENT_TYPE);
    }

    void setContentType(String contentType) {
        set(CONTENT_TYPE, contentType);
    }

    public void setMessageTimeSent(String s) {
        set(TIME_SENT, s);
    }

    void acquireUniqueId() {
        String id = UniqueIdGenerator.INSTANCE.getNextDocumentId();
        set(UNIQUE_ID, id);
        //setMasterDuplicate();
    }

    public String getUniqueId() {
        return get(UNIQUE_ID);
    }

    void setMessageId(String messageId) {
        set(MESSAGE_ID, messageId);
    }

    void setReferencedMessageIds(String references) {
        set(REFERENCES, references);
    }

    public String getMessageId() {
        return get(MESSAGE_ID);
    }

    public String getReferences() {
        return get(REFERENCES);
    }

    /**
     * Similar to super.add(), but with an additional return type, for fluent
     * interface pattern.
     *
     * @param key   key in the hashmap to be added.
     * @param value value in the hashmap to be added.
     */
    void addField(String key, String value) {
        add(key, value);
    }

    /**
     * Does the document have attachments?
     *
     * @return true if yes, false if no.
     */
    public boolean hasAttachments() {
        return isPropertyTrue(HAS_ATTACHMENTS);
    }

    /**
     * Set a flag to indicate if the document has attachments.
     *
     * @param b true if it has attachments, false if it does not.
     */
    void setHasAttachments(boolean b) {
        setProperty(HAS_ATTACHMENTS, b);
    }

    /**
     * Does the document have a parent?
     *
     * @return true if yes, false if no.
     */
    public boolean hasParent() {
        return isPropertyTrue(HAS_PARENT);
    }

    /**
     * Set a flag to indicate if the document has a parent.
     *
     * @param b true if it has a parent, false if it does not.
     */
    void setHasParent(boolean b) {
        setProperty(HAS_PARENT, b);
    }

    /**
     * Return the true or false for a specific property. All true properties in
     * the Project setup are coded with either property-key=yes. Anything else,
     * such as key absent, value="no" or value = "false" results in false
     *
     * @param propertyKey the key we are checking
     * @return true if the property is present and its values is "true", and
     * false otherwise
     */
    private boolean isPropertyTrue(String propertyKey) {
        String propertyValue = get(propertyKey);
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        } else {
            return false;
        }
    }

    /**
     * Convenience function to set boolean properties as strings.
     *
     * @param propertyKey key to set.
     * @param b           for true, set "true", for false, remove the key from the
     *                    underlying map.
     */
    private void setProperty(String propertyKey, boolean b) {
        if (b) {
            set(propertyKey, Boolean.TRUE.toString());
        } else {
            remove(propertyKey);
        }
    }

    /**
     * Compare the document's hash with the contents of the duplicate tracker If
     * the document with this hash has not been seen before, then it's master
     * field is empty Otherwise, its master is set id of the first document
     * (called 'master')
     */
    public void setMasterDuplicate() {
        String hash = getHash();
        assert (hash != null);
        String masterId = DuplicatesTracker.INSTANCE.getMasterId(hash, getUniqueId());
        if (!masterId.equals(getUniqueId())) {
            set(MASTER_DUPLICATE, masterId);
        }

    }

    public static String TEXT_LINK() {
        return TEXT_LINK;
    }

    public static String getLinkNative() {
        return LINK_NATIVE;
    }

    public static String getLinkException() {
        return LINK_EXCEPTION;
    }
}
