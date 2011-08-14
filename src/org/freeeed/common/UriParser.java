/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.common;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @author mark
 */
public class UriParser {
    public static void main(String argv[]) {
        UriParser instance = new UriParser();
        String uriStr = "http://shmsoft.com/index.hml";
        instance.parseUri(uriStr);
        uriStr = "file:///home/mark/derby.log";
        instance.parseUri(uriStr);
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
}
