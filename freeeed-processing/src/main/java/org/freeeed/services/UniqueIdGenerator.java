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
package org.freeeed.services;

import org.freeeed.main.ParameterProcessing;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author mark
 * Generates unique ids for documents.
 * Current implementation is a singleton going from 1 to N.
 */
public enum UniqueIdGenerator {

    INSTANCE;

    private AtomicLong uniqueDocumentId = new AtomicLong();
    private AtomicLong uniquePSTId = new AtomicLong();
    private AtomicLong uniqueEMLId = new AtomicLong();
    private AtomicLong uniqueZIPFolderId = new AtomicLong();
    private AtomicLong uniqueZIPFileId = new AtomicLong();

    public String getNextDocumentId() {
        uniqueDocumentId.incrementAndGet();
        return ParameterProcessing.DOCTFormat.format(uniqueDocumentId);
    }

    public String getNextPSTId() {
        uniquePSTId.incrementAndGet();
        return ParameterProcessing.PSTFormat.format(uniquePSTId);
    }

    public String getNextEMLId() {
        uniqueEMLId.incrementAndGet();
        return ParameterProcessing.EMLFormat.format(uniqueEMLId);
    }

    public String getNextZIPFolderId() {
        uniqueZIPFolderId.incrementAndGet();
        return ParameterProcessing.ZIPFolderFormat.format(uniqueZIPFolderId);
    }

    public String getNextZIPFileId() {
        uniqueZIPFileId.incrementAndGet();
        return ParameterProcessing.ZIPFileFormat.format(uniqueZIPFileId);
    }


    public void reset() {
        uniqueDocumentId.set(0);
    }
}
