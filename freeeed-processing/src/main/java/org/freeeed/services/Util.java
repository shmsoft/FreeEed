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
package org.freeeed.services;

import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.Charset;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.tika.metadata.Metadata;
import org.freeeed.mail.EmailProperties;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.util.AutomaticUICaseCreator;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    public static byte[] getFileContent(String fileName) throws IOException {
        return Files.toByteArray(new File(fileName));
    }

    /**
     * Removing control characters from input because for solr text.
     *
     * @param inString
     * @return
     */
    public static String removeNonUtf8CompliantCharacters(final String inString) {
        if (null == inString) return null;
        byte[] byteArr = inString.getBytes();
        for (int i = 0; i < byteArr.length; i++) {
            byte ch = byteArr[i];
            // remove any characters outside the valid UTF-8 range as well as all control characters
            if (!(ch < 0x00FD && ch > 0x001F) || ch == '&' || ch == '#') {
                byteArr[i] = ' ';
            }
        }
        return new String(byteArr);
    }

    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new RuntimeException(file.getName() + " is too large");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    /**
     * @param fileName
     * @return content of the file
     */
    public static String readTextFile(String fileName) throws IOException {
        return Files.toString(new File(fileName), Charset.defaultCharset());
    }

    public static void writeTextFile(String fileName, String content) throws IOException {
        Files.write(content, new File(fileName), Charset.defaultCharset());
    }

    public static void appendToTextFile(String fileName, String content) throws IOException {
        Files.append(content, new File(fileName), Charset.defaultCharset());
    }

    public static String toString(Metadata metadata) {
        StringBuilder builder = new StringBuilder();
        String[] names = metadata.names();
        for (String name : names) {
            builder.append(name).append("=").append(metadata.get(name)).append(ParameterProcessing.NL);
        }
        return builder.toString();
    }

    public static boolean isSystemFile(Metadata metadata) {
        return "application/octet-stream".equalsIgnoreCase(
                metadata.get("Content-Type"));
    }

    public static int countLines(String filename) throws IOException {
        int cnt;
        try (LineNumberReader reader = new LineNumberReader(new FileReader(filename))) {
            String lineRead = "";
            while ((lineRead = reader.readLine()) != null) {
            }
            cnt = reader.getLineNumber();
        }
        return cnt;
    }

    public static String arrayToString(String[] strings) {
        StringBuilder builder = new StringBuilder();
        for (String str : strings) {
            builder.append(str).append("\n");
        }
        return builder.toString();
    }

    /**
     * Delete directory with everything underneath. Note that in the case of
     * *nix we use 'rm -fr my-dir', because of the known problems with recursive
     * deletes, and because 'rm -fr' is probably faster.
     *
     * @param dir directory to delete.
     * @throws IOException on any problem with delete.
     */
    public static void deleteDirectory(File dir) throws IOException {
        if (OsUtil.isNix()) {
            OsUtil.runCommand("rm -fr " + dir.getPath());
        } else {
            FileUtils.deleteDirectory(dir);
        }
    }

    public static MD5Hash createKeyHash(File file, Metadata metadata) throws IOException {
        String extension = Util.getExtension(file.getName());

        if ("eml".equalsIgnoreCase(extension)) {
            assert (metadata != null);
            String hashNames = EmailProperties.getInstance().getProperty(EmailProperties.EMAIL_HASH_NAMES);
            String[] hashNamesArr = hashNames.split(",");

            StringBuilder data = new StringBuilder();

            for (String hashName : hashNamesArr) {
                String value = metadata.get(hashName);
                if (value != null) {
                    data.append(value);
                    data.append(" ");
                }
            }
            return MD5Hash.digest(data.toString());
        } else {
            MD5Hash key;
            try ( //use MD5 of the input file as Hadoop key
                    FileInputStream fileInputStream = new FileInputStream(file)) {
                key = MD5Hash.digest(fileInputStream);
            }
            return key;
        }
    }

    /**
     * Scaffolding for verifying code. Not needed after the code is working.
     * @param args
     */
    public static void main (String [] args) {
        System.out.println("What would you like?");
    }
}
