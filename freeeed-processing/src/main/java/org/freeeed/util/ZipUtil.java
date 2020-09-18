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
package org.freeeed.util;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    /**
     * Create a zip file from the given directory to zip.
     *
     * @param zipFileName
     * @param directoryToZip
     * @throws IOException
     */
    public static void createZipFile(String zipFileName, String directoryToZip) throws IOException {
        //create object of FileOutputStream
        FileOutputStream fout = new FileOutputStream(zipFileName);

        //create File object from source directory
        try ( //create object of ZipOutputStream from FileOutputStream
              ZipOutputStream zout = new ZipOutputStream(fout)) {
            //create File object from source directory
            File fileSource = new File(directoryToZip);
            addDirectory(zout, fileSource);
            //close the ZipOutputStream
        }
    }

    private static void addDirectory(ZipOutputStream zout, File fileSource) throws IOException {
        File[] files = fileSource.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                addDirectory(zout, file);
                continue;
            }
            byte[] buffer = new byte[1024];
            try (FileInputStream fin = new FileInputStream(file)) {
                zout.putNextEntry(new ZipEntry(file.getName()));
                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }
                zout.closeEntry();
            }
        }
    }

    /**
     * Unzip a given zip file to a specified output directory.
     *
     * @param zipFileName
     * @param outputDir
     * @throws IOException
     */
    public static void unzipFile(String zipFileName, String outputDir)
            throws IOException {

        ZipFile zipFile = new ZipFile(new File(zipFileName));
        @SuppressWarnings("rawtypes")
        Enumeration e = zipFile.entries();

        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) e.nextElement();
            File destinationFilePath = new File(outputDir, entry.getName());

            // create directories if required.
            destinationFilePath.getParentFile().mkdirs();

            // if the entry is directory, leave it. Otherwise extract it.
            if (entry.isDirectory()) {
            } else {
                try ( /*
                 * Get the InputStream for current entry of the zip file using
                 *
                 * InputStream getInputStream(Entry entry) method.
                 */ BufferedInputStream bis = new BufferedInputStream(
                        zipFile.getInputStream(entry))) {
                    int b;
                    byte[] buffer = new byte[1024];
                    /*
                     * read the current entry from the zip file, extract it and
                     * write the extracted file.
                     */
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    try (BufferedOutputStream bos = new BufferedOutputStream(fos, 1024)) {
                        while ((b = bis.read(buffer, 0, 1024)) != -1) {
                            bos.write(buffer, 0, b);
                        }   // flush the output stream and close it.
                        bos.flush();
                        // close the input stream.
                    }
                }
            }
        }
    }
    public static void mergeZips(String[] inputZipPaths, String outputZipFile) throws IOException {

    }
}
