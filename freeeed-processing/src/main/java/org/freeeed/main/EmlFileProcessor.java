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
import java.util.logging.Logger;

import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.freeeed.util.LogFactory;


/**
 * Process email files
 */
public class EmlFileProcessor extends FileProcessor {
    private final static Logger LOGGER = LogFactory.getLogger(EmlFileProcessor.class.getName());
    
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
    public void process(boolean hasAttachments, String hash) throws IOException, InterruptedException {
        String emailPath = getSingleFileName();
        String emailName = new File(emailPath).getName();
        // TODO this is a little more complex, there are attachments without extensions
        // if the file already has an extension - then it is an attachment
        String ext = Util.getExtension(emailName);
        if (ext.isEmpty()) {
            emailName += ".eml";
        }
        
        LOGGER.fine("Processing eml file with path: " + emailPath + ", name: " + emailName);
        processFileEntry(new DiscoveryFile(emailPath, emailName, hasAttachments, hash));
    }

    @Override
    String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        // Point at the ACTUAL on-disk file, not a name rebuilt from getRealFileName().
        // On Windows the JPST extractor (PstProcessor.extractEmails) writes messages with no
        // extension (e.g. "1"), while EmlFileProcessor.process appends ".eml" to realFileName --
        // so rebuilding from realFileName ("1.eml") points at a file that does not exist and
        // Review reports "native file not found" (issue #580). The physical path is correct on
        // both platforms (on *nix, readpst -e already writes "1.eml", so this is unchanged there).
        return discoveryFile.getPath().getPath().substring(Settings.getSettings().getPSTDir().length() + 1);
    }

    @Override
    String getDocumentFullPath(DiscoveryFile discoveryFile) {
        return discoveryFile.getPath().getAbsolutePath();
    }


}
