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
package org.freeeed.ocr.benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
@Deprecated
public class BrownCorpusTextGenerator implements ITextGenerator {

    private String samplesDir;
    private String[] files;
    private Random random;
    private BrownReader brownReader;

    public BrownCorpusTextGenerator(String templateArchiveFile,
            String samplesDir) {
        
        File f = new File(samplesDir);
        if (f.exists()) {
            System.out.println("Samples already extracted");
        } else {
            f.mkdir();

            try {
                doUnzip(templateArchiveFile, samplesDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.files = f.list();
        this.samplesDir = samplesDir;
        this.random = new Random();
        this.brownReader = new BrownReader();
    }

    @Override
    public String getRandomText() {
        int index = random.nextInt(files.length);
        return getText(index);
    }

    @Override
    public String getText(int index) {
        String file = samplesDir + File.separatorChar + files[index];

        int count = 0;
        List<String> lines = brownReader.readCorpus(file);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line);
            if (count < lines.size() - 1) {
                String next = lines.get(count + 1);
                if (!(next.trim().equals(".") || next.trim().equals(","))) {
                    sb.append(" ");
                }
            }

            count++;
        }

        return sb.toString();
    }

    @Override
    public int getTextsCount() {
        return files.length;
    }

    public final void doUnzip(String inputZip, String destinationDirectory)
            throws IOException {

        int BUFFER = 2048;
        List<String> zipFiles = new ArrayList<String>();
        File sourceZipFile = new File(inputZip);
        File unzipDestinationDirectory = new File(destinationDirectory);
        unzipDestinationDirectory.mkdir();

        ZipFile zipFile;
        // Open Zip file for reading
        zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

        // Create an enumeration of the entries in the zip file
        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();

        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();

            String currentEntry = entry.getName();

            File destFile = new File(unzipDestinationDirectory, currentEntry);
            destFile = new File(unzipDestinationDirectory, destFile.getName());

            if (currentEntry.endsWith(".zip")) {
                zipFiles.add(destFile.getAbsolutePath());
            }

            // grab file's parent directory structure
            File destinationParent = destFile.getParentFile();

            // create the parent directory structure if needed
            destinationParent.mkdirs();

            try {
                // extract file if not a directory
                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(
                            zipFile.getInputStream(entry));
                    int currentByte;
                    // establish buffer for writing file
                    byte data[] = new byte[BUFFER];

                    // write the current file to disk
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos,
                            BUFFER);

                    // read and write until last byte is encountered
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            } catch (IOException ioe) {
                System.out.println("Problem doing unzip: " + ioe.getMessage());
            }
        }
        zipFile.close();

        for (Iterator<String> iter = zipFiles.iterator(); iter.hasNext();) {
            String zipName = (String) iter.next();
            doUnzip(zipName, destinationDirectory + File.separatorChar
                    + zipName.substring(0, zipName.lastIndexOf(".zip")));
        }

    }
}
