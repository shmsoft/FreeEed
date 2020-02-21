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

import com.google.common.io.Files;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.metadata.Metadata;
import org.freeeed.mail.EmailProperties;
import org.freeeed.main.DiscoveryFile;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Project;

public class Util {

    public static String getCustodianFromPath(File f) {
        String[] pathParts = f.getPath().split(System.getProperty("file.separator"));
        int custodianIndex = 0;
        for (String pathPart : pathParts) {
            custodianIndex++;
            if (pathPart.equals("staging")) {
                break;
            }
        }
        return pathParts[custodianIndex];
    }

    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    public static byte[] getFileContent(String fileName) throws IOException {
        return Files.toByteArray(new File(fileName));
    }

    public static String toString(Metadata metadata) {
        StringBuilder builder = new StringBuilder();
        String[] names = metadata.names();
        for (String name : names) {
            builder.append(name).append("=").append(metadata.get(name)).append(ParameterProcessing.NL);
        }
        return builder.toString();
    }

    public static boolean isSystemFile(DiscoveryFile discoveryFile) {
        String ext = Util.getExtension(discoveryFile.getRealFileName());
        List<String> systemExt = Arrays.asList("exe", "msi");
        return systemExt.contains(ext);
    }


    /**
     * Delete directory with everything underneath.
     *
     * @param dir directory to delete.
     * @throws IOException on any problem with delete.
     */
    public static void deleteDirectory(File dir) throws IOException {
        FileUtils.deleteDirectory(dir);
    }

    public static String createKeyHash(File file, Metadata metadata) throws IOException {
        String extension = Util.getExtension(file.getName());
        String hash = null;
        if ("eml".equalsIgnoreCase(extension) && false) {
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
            System.out.println(data.toString());
            // return MD5Hash.digest(data.toString());
        } else {
            try {
                hash = getFileChecksum(file);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return hash;
    }

    private static String getFileChecksum(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);

        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount;

        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        //close the stream; We don't need it now.
        fis.close();
        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

    private static long dirSize(Path path) {
        long size = 0;
        try {
            DirectoryStream ds = java.nio.file.Files.newDirectoryStream(path);
            for (Object o : ds) {
                Path p = (Path) o;
                if (java.nio.file.Files.isDirectory(p)) {
                    size += dirSize(p);
                } else {
                    size += java.nio.file.Files.size(p);
                }
            }
        } catch (IOException e) {
            //LOGGER.error("Dir size calculation error", e);
        }
        return size;
    }

    /**
     * This is a recursive function going through all subdirectories It uses the
     * class variable totalSize to keep track through recursions
     *
     * @throws IOException
     */
    public static long calculateSize() throws IOException {
        Project project = Project.getCurrentProject();
        String[] dirs = project.getInputs();
        long totalSize = 0;

        for (String dir : dirs) {
            Path path = Paths.get(dir);
            if (java.nio.file.Files.exists(path)) {
                if (java.nio.file.Files.isDirectory(path)) {
                    // TODO check for efficiency
                    totalSize += dirSize(path);
                } else {
                    totalSize += java.nio.file.Files.size(path);
                }
            }
        }

        return totalSize;
    }
}
