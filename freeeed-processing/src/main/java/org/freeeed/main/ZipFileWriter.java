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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opens a zip file for output and writes the text, native files, and exceptions
 * into it.
 */
public class ZipFileWriter {

    private static final Logger log = LoggerFactory.getLogger(ZipFileWriter.class);

    private String rootDir;
    private String zipFileName;
    private ZipOutputStream zipOutputStream;
    private FileOutputStream fileOutputStream;

    public ZipFileWriter() {
    }

    public void setup() {
        String custodian = Project.getCurrentProject().getCurrentCustodian();
        String custodianExt = custodian.trim().length() > 0 ? "_" + custodian : "";
        if (Project.getCurrentProject().isEnvLocal()) {
            rootDir = Project.getCurrentProject().getResultsDir();
            zipFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.PRODUCTION_FILE_NAME
                    + custodianExt + ".zip";
        } else {
            rootDir = ParameterProcessing.TMP_DIR_HADOOP
                    + System.getProperty("file.separator") + "output";
            zipFileName = rootDir
                    + System.getProperty("file.separator")
                    + Project.PRODUCTION_FILE_NAME
                    + custodianExt + ".zip";
        }
        new File(rootDir).mkdir();

        log.debug("Filename: " + zipFileName + ", Root dir: " + rootDir);
    }

    public void openZipForWriting() throws IOException {
        new File(new File(zipFileName).getParent()).mkdirs();
        fileOutputStream = new FileOutputStream(zipFileName);
        zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
    }

    public void closeZip() throws IOException {
        zipOutputStream.close();
        fileOutputStream.close();
    }

    public void addTextFile(String entryName, String textContent) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(zipEntry);
        if (textContent == null) {
            textContent = "No text extracted";
        }
        zipOutputStream.write(textContent.getBytes());
    }

    public void addBinaryFile(String entryName, byte[] fileContent, int length) throws IOException {
        ZipEntry zipEntry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(fileContent, 0, length);
    }

    public String getZipFileName() {
        return zipFileName;
    }
}
