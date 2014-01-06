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
    private String mrkey;
    private String realFileName;
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
    public DiscoveryFile(String pathStr, String realFileName, boolean hasAttachments, MD5Hash hash) {
        this.path = new File(pathStr);
        this.realFileName = realFileName;
        this.hasAttachments = hasAttachments;
        this.hash = hash;
    }

    /**
     * @return the path
     */
    public File getPath() {
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
    public void setHash(MD5Hash hash) {
        this.hash = hash;
    }

    /**
     * @return the realFileName
     */
    public String getRealFileName() {
        return realFileName;
    }

    /**
     * @param realFileName the realFileName to set
     */
    protected void setRealFileName(String realFileName) {
        this.realFileName = realFileName;
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

    /**
     * @return the mrkey
     */
    protected String getMrkey() {
        return mrkey;
    }

    /**
     * @param mrkey the mrkey to set
     */
    protected void setMrkey(String mrkey) {
        this.mrkey = mrkey;
    }
}
