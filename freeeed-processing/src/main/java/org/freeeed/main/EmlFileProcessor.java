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
import java.io.IOException;
import org.apache.hadoop.io.MD5Hash;

import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Process email files
 */
public class EmlFileProcessor extends FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EmlFileProcessor.class);
    
    /**
     * Constructor
     * 
     * @param singleFileName
     * @param metadataWriter
     * @param luceneIndex
     */
    public EmlFileProcessor(String singleFileName, MetadataWriter metadataWriter, LuceneIndex luceneIndex) {
        super(metadataWriter, luceneIndex);        
        this.singleFileName = singleFileName;
    }

    /**
     * Process file
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void process(boolean hasAttachments, MD5Hash hash) throws IOException, InterruptedException {
        String emailPath = getSingleFileName();
        String emailName = new File(emailPath).getName();
        // TODO this is a little more complex, there are attachments without extensions
        // if the file already has an extension - then it is an attachment
        String ext = Util.getExtension(emailName);
        if (ext.isEmpty()) {
            emailName += ".eml";
        }
        
        logger.debug("Processing eml file with path: " + emailPath + ", name: " + emailName);                
        processFileEntry(new DiscoveryFile(emailPath, emailName, hasAttachments, hash));
    }

    @Override
    String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        String pathToEmail = discoveryFile.getPath().getPath().substring(Settings.getSettings().getPSTDir().length() + 1);
        return new File(pathToEmail).getParent() + File.separator + discoveryFile.getRealFileName();
    }
}
