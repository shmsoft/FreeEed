package org.freeeed.SearchEngine;

import org.apache.tika.metadata.Metadata;

public interface SearchEngine extends Runnable {

    void connect();
    void addMetaData(Metadata metadata);
    void disconnect();
}
