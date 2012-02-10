package org.freeeed.main;

import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import org.apache.tika.metadata.Metadata;

public class Util {

    public enum ENV {
        LOCAL, HADOOP, S3
    };
    static private ENV env = ENV.LOCAL;

    static public void setEnv(String runWhere) {
        if (ENV.LOCAL.toString().equalsIgnoreCase(runWhere)) {
            env = ENV.LOCAL;
        } else if (ENV.HADOOP.toString().equalsIgnoreCase(runWhere)) {
            env = ENV.HADOOP;
        } else if (ENV.S3.toString().equalsIgnoreCase(runWhere)) {
            env = ENV.S3;
        } else {
            throw new RuntimeException("Unknown environment: " + runWhere);
        }
    }
    static public ENV getEnv() {
        return env;
    }
    public static final String NL = System.getProperty("line.separator");

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

    public static byte[] getFileContent(String fileName) throws IOException {
        return Files.toByteArray(new File(fileName));
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
            builder.append(name).append("=").append(metadata.get(name)).append(NL);
        }
        return builder.toString();
    }

    public static Properties propsFromString(String str) {
        Properties props = new Properties();
        if (str == null) {
            return props;
        }
        try {
            props.load(new StringReader(str.substring(1, str.length() - 1).replace(", ", "\n")));
            HashMap<String, String> map2 = new HashMap<String, String>();
            for (java.util.Map.Entry<Object, Object> e : props.entrySet()) {
                map2.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return props;
    }
//    public static FreeEedConfiguration configFromString(String str) {
//        FreeEedConfiguration conf = new FreeEedConfiguration();
//        Properties props = propsFromString(str);
//        Set keys = props.keySet();
//        Iterator iter = keys.iterator();
//        while (iter.hasNext()) {
//            String key = (String) iter.next();
//            String value = props.getProperty(key);
//            conf.setProperty(key, value);
//        }
//        return conf;
//    }
}
