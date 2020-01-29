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

import org.apache.lucene.queryparser.classic.ParseException;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.services.Settings;
import org.freeeed.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Process email files
 */
public class EmlFileProcessor extends FileProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmlFileProcessor.class);

    public EmlFileProcessor(MetadataWriter metadataWriter, LuceneIndex luceneIndex, DiscoveryFile discoveryFile) {
        this.metadataWriter = metadataWriter;
        this.luceneIndex = luceneIndex;
        this.discoveryFile = discoveryFile;
    }

    public static boolean isEml(DiscoveryFile discoveryFile) {
        boolean isEml = false;
        String ext = Util.getExtension(discoveryFile.getRealFileName());
        if ("eml".equalsIgnoreCase(ext)) {
            isEml = true;
        }
        //LOGGER.trace("{} is {}", discoveryFile.getRealFileName(), (isEml) ? "EML" : "NOT EML");
        return isEml;
    }

    @Override
    public String getSingleFileName() {
        return discoveryFile.getRealFileName();
    }

    @Override
    public void run() {
        String emailPath = getSingleFileName();
        String emailName = new File(emailPath).getName();
        // TODO this is a little more complex, there are attachments without extensions
        // if the file already has an extension - then it is an attachment
        String ext = Util.getExtension(emailPath);
        if (ext.isEmpty()) {
            emailPath += ".eml";
        }
        //LOGGER.debug("Processing eml file with path: " + emailPath + ", name: " + emailName);
        // update application log
        //LOGGER.trace("Processing file: {}", discoveryFile.getRealFileName());
        // set to true if file matches any query params
        boolean isResponsive = false;
        // exception message to place in output if error occurs
        String exceptionMessage = null;
        // ImageTextParser metadata, derived from Tika metadata class
        DocumentMetadata metadata = new DocumentMetadata();
        discoveryFile.setMetadata(metadata);
        String extension = Util.getExtension(discoveryFile.getRealFileName());
        if ("jl".equalsIgnoreCase(extension)) {
            extractJlFields(discoveryFile);
        }
        try {
            metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
            metadata.setHasAttachments(discoveryFile.isHasAttachments());
            metadata.setHasParent(discoveryFile.isHasParent());
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            // TODO discoveryFile has the pointer to the same metadata - simplify this
            extractMetadata(discoveryFile, metadata);
            /*
            if (project.isRemoveSystemFiles() && Util.isSystemFile(metadata)) {
                LOGGER.info("File {} is recognized as system file and is not processed further",
                        discoveryFile.getPath().getPath());
                return;
            }
            metadata.setCustodian(project.getCurrentCustodian());
            */
            // add Hash to metadata
            MD5Hash hash = Util.createKeyHash(discoveryFile.getPath(), metadata);
            metadata.setHash(hash.toString());
            metadata.acquireUniqueId();
            // search through Tika results using Lucene
            isResponsive = isResponsive(metadata);
            if (isResponsive) {
                addToES(metadata);
            }
        } catch (IOException | ParseException e) {
            LOGGER.warn("Exception processing file ", e);
            exceptionMessage = e.getMessage();
        }
        // update exception message if error
        if (exceptionMessage != null) {
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }
        if (isResponsive || exceptionMessage != null) {
            createImage(discoveryFile);
            if (isPreview()) {
                try {
                    createHtmlForDocument(discoveryFile);
                } catch (Exception e) {
                    metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                }
            }
            try {
                writeMetadata(discoveryFile, metadata);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //LOGGER.trace("Is the file responsive: {}", isResponsive);
    }

    @Override
    public String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        String pathToEmail = discoveryFile.getPath().getPath().substring(Settings.getSettings().getPSTDir().length() + 1);
        return new File(pathToEmail).getParent() + File.separator + discoveryFile.getRealFileName();
    }


}
