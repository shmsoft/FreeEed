package org.freeeed.main;

public class Util {

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
}
