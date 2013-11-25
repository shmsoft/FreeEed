package org.freeeed.main;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Message;

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
    
    public String getOriginalPath() {
        return get(DOCUMENT_ORIGINAL_PATH);
    }
    public void setOriginalPath(String originalPath) {
        set(DOCUMENT_ORIGINAL_PATH, originalPath);
    }
    public String getFrom() {
        return get(Message.MESSAGE_FROM);
    }
    public String getDocumentText() {
        return get(DOCUMENT_TEXT);
    }
    public void setDocumentText(String documentText) {
        set(DOCUMENT_TEXT, documentText);
    }
}
