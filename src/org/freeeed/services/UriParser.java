package org.freeeed.services;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author mark
 * Test code for reading http, https, ftp, etc.
 * 
 * Following this advice, http://www.nsftools.com/tips/JavaFtp.htm
 * So for example for ftp, ftp://username:password@ftp.yoursite.com
 */
public class UriParser {

    public static void main(String argv[]) {
        UriParser instance = new UriParser();
        String uriStr = "http://shmsoft.com/index.php";
        instance.parseUri(uriStr);
        instance.readUri(uriStr);

        uriStr = "file:///home/mark/derby.log";
        instance.parseUri(uriStr);
        instance.readUri(uriStr);
    }

    public void parseUri(String uriStr) {
        try {
            URI uri = new URI(uriStr);
            System.out.println("Authority: " + uri.getAuthority());
            System.out.println("Scheme: " + uri.getScheme());
            System.out.println("Host: " + uri.getHost());
            System.out.println("Path: " + uri.getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace(System.out);
        }
    }

    public void readUri(String uriStr) {
        try {
            URL url = new URL(uriStr);
            URLConnection con = url.openConnection();
            BufferedInputStream in =
                    new BufferedInputStream(con.getInputStream());
            FileOutputStream out =
                    new FileOutputStream("/home/mark/output");

            int i = 0;
            byte[] bytesIn = new byte[1024];
            while ((i = in.read(bytesIn)) >= 0) {
                out.write(bytesIn, 0, i);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }
}
