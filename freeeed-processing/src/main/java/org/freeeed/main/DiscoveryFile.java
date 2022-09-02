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


/**
 * Container to pass around additional information about a file needed in discovery.
 *
 * @author mark
 */
public class DiscoveryFile {

    private File path;
    private String hash;
    private String mrkey;
    private String realFileName;
    private boolean hasAttachments;
    private boolean hasParent;
    private String custodian;

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
     * that is, only one level of inheritance is recorded.
     * @param hash
     */
    public DiscoveryFile(String pathStr, String realFileName, boolean hasAttachments, String hash) {
        this.path = new File(pathStr);
        this.realFileName = realFileName;
        this.hasAttachments = hasAttachments;
        this.hash = hash;
        this.hasParent = (hash != null);
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
    protected String getHash() {
        return hash;
    }

    /**
     * @param hash the hash to set
     */
    public void setHash(String hash) {
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

    /**
     * @return the hasParent
     */
    public boolean isHasParent() {
        return hasParent;
    }

    /**
     * @param hasParent the hasParent to set
     */
    public void setHasParent(boolean hasParent) {
        if (hasParent) {
            this.hasParent = true;
        }
        else {
            this.hasParent = false;
        }
    }
    
    /**
     * @return the file size in bytes
     */
    public long getFileSize() {
        if (path != null) {
            return path.length();
        }
        
        return 0;
    }

    /**
     * @return the custodian
     */
    public String getCustodian() {
        return custodian;
    }

    /**
     * @param custodian the custodian to set
     */
    public void setCustodian(String custodian) {
        this.custodian = custodian;
    }
}
