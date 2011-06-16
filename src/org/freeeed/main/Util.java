package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class Util {

    private static final int BUFFER_SIZE = 4096;

    public static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        if (dot < 0) {
            return "";
        }
        String extension = fileName.substring(dot + 1);
        if (extension.length() > 10) {
            return "";
        }
        return extension;
    }

    public static byte[] getFileContent(String fileName) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(fileName));
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int n = in.read(buffer, 0, BUFFER_SIZE);
            while (n >= 0) {
                out.write(buffer, 0, n);
                n = in.read(buffer, 0, BUFFER_SIZE);
            }
        } catch (Exception e) {
            // TODO better error handling
            e.printStackTrace(System.out);
        } finally { // always close input stream
            if (in != null) {
                try { in.close(); } catch (Exception e) {}
            }
        }
        return out.toByteArray();
    }
}
