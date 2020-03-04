package org.freeeed.main;
public class SystemFileProcessor extends FileProcessor {

    public SystemFileProcessor(DiscoveryFile discoveryFile) {
        this.discoveryFile = discoveryFile;
    }

    @Override
    public void run() {
        DocumentMetadata metadata = new DocumentMetadata();
        discoveryFile.setMetadata(metadata);
        metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
        metadata.setHasAttachments(discoveryFile.isHasAttachments());
        metadata.setHasParent(discoveryFile.isHasParent());
        // MD5Hash hash = Util.createKeyHash(discoveryFile.getPath(), metadata);
        // metadata.setHash(hash.toString());
        metadata.acquireUniqueId();
        metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, "System File");
        writeMetadata();
    }
}
