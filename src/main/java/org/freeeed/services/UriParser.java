/*    
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
