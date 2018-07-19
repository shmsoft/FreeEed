package org.freeeed.main.processinginvoker;

import org.apache.hadoop.io.MD5Hash;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;

public class EmailProcessingArg {

    private String emailDir;
    private boolean hasAttachments;
    private MD5Hash hash;
    private MetadataWriter metadataWriter;
    private LuceneIndex luceneIndex;

    public EmailProcessingArg(String emailDir, boolean hasAttachments, MD5Hash hash, MetadataWriter metadataWriter, LuceneIndex luceneIndex) {
        this.emailDir = emailDir;
        this.hasAttachments = hasAttachments;
        this.hash = hash;
        this.metadataWriter = metadataWriter;
        this.luceneIndex = luceneIndex;
    }

    public String getEmailDir() {
        return emailDir;
    }

    public void setEmailDir(String emailDir) {
        this.emailDir = emailDir;
    }

    public boolean hasAttachments() {
        return hasAttachments;
    }

    public void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }

    public MD5Hash getHash() {
        return hash;
    }

    public void setHash(MD5Hash hash) {
        this.hash = hash;
    }

    public MetadataWriter getMetadataWriter() {
        return metadataWriter;
    }

    public void setMetadataWriter(MetadataWriter metadataWriter) {
        this.metadataWriter = metadataWriter;
    }

    public LuceneIndex getLuceneIndex() {
        return luceneIndex;
    }

    public void setLuceneIndex(LuceneIndex luceneIndex) {
        this.luceneIndex = luceneIndex;
    }

    @Override
    public String toString() {
        return "EmailProcessingArg{" +
                "emailDir='" + emailDir + '\'' +
                ", hasAttachments=" + hasAttachments +
                ", hash=" + hash +
                ", metadataWriter=" + metadataWriter +
                ", luceneIndex=" + luceneIndex +
                '}';
    }
}
