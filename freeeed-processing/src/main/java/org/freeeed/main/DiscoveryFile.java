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
package org.freeeed.main;

import java.io.File;
import org.apache.hadoop.io.MD5Hash;

/**
 * Container to pass around additional information about a file needed in discovery.
 *
 * @author mark
 */
public class DiscoveryFile {

    private File path;
    private MD5Hash hash;
    private String realFileName;
    private File parentPath;
    private MD5Hash parentHash;
    private boolean hasAttachments;

    /**
     * Constructor with two parameters and the rest defaults: no attachments or parents.
     *
     * @param pathStr path to the file.
     * @param realFileName original file name.
     */
    public DiscoveryFile(String pathStr, String realFileName) {
        this.path = new File(pathStr);
        this.realFileName = realFileName;
    }

    /**
     * Constructor with all direct parameters.
     *
     * @param pathStr path to the file.
     * @param realFileName original file name.
     * @param hasAttachments does it have attachments or no.
     * @param parentPath if it is an attachment, path to the parent. A parent of a parent is also considered a parent,
     * that is, only one level of inheritance is recorded.
     */
    public DiscoveryFile(String pathStr, String realFileName, boolean hasAttachments, File parentPath) {
        this.path = new File(pathStr);
        this.realFileName = realFileName;
        this.hasAttachments = hasAttachments;
        this.parentPath = parentPath;
    }

    /**
     * @return the path
     */
    protected File getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    protected void setPath(File path) {
        this.path = path;
    }

    /**
     * @return the hash
     */
    protected MD5Hash getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    protected void setHash(MD5Hash hash) {
        this.hash = hash;
    }

    /**
     * @return the realFileName
     */
    protected String getRealFileName() {
        return realFileName;
    }

    /**
     * @param realFileName the realFileName to set
     */
    protected void setRealFileName(String realFileName) {
        this.realFileName = realFileName;
    }

    /**
     * @return the parentPath
     */
    protected File getParentPath() {
        return parentPath;
    }

    /**
     * @param parentPath the parentPath to set
     */
    protected void setParentPath(File parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * @return the parentHash
     */
    protected MD5Hash getParentHash() {
        return parentHash;
    }

    /**
     * @param parentHash the parentHash to set
     */
    protected void setParentHash(MD5Hash parentHash) {
        this.parentHash = parentHash;
    }

    /**
     * @return the hasAttachments
     */
    protected boolean isHasAttachments() {
        return hasAttachments;
    }

    /**
     * @param hasAttachments the hasAttachments to set
     */
    protected void setHasAttachments(boolean hasAttachments) {
        this.hasAttachments = hasAttachments;
    }
}
