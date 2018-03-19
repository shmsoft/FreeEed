/**
 * Copyright SHMsoft, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.services;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks duplicate files.
 * Current implementation is build on an in-memory a hash table,
 * both may be changed in the future
 *
 * @author mark
 */
public enum DuplicatesTracker {

    INSTANCE;
    /**
     * Using ConcurrentHashMap because multiple threads may access the table.
     */
    private ConcurrentHashMap<String, // hash key of the document
            String // unique ID of the document
            > master = new ConcurrentHashMap<>();

    /**
     * @param hash     Hash of the document
     * @param uniqueId ID of the document
     * @return unique ID of the master document, it can be equal to the input ID
     * which means the doc is its own master, not a dupe. If it is different,
     * this document is a duplicate
     */
    public String getMasterId(String hash, String uniqueId) {
        return master.computeIfAbsent(hash, k -> uniqueId);
    }

    /**
     * Delete all records from the duplicates store
     */
    public void reset() {
        master.clear();
    }
}
