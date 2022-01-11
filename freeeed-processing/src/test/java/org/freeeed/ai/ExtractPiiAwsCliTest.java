/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author mark
 * For testing
 * aws comprehend detect-pii-entities \
 * --text "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010" \
 * --language-code en
 */
public class ExtractPiiAwsCliTest {
    //@Test
    public void testAwsPii() {
        System.out.println("ExtractPiiAwsTest");
        String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";
        String response = new ExtractPiiAwsCLI().extractPiiAsString(data);
        System.out.println(response);
        // 24 characters is empty response
        assertTrue(response.length() > 24);
    }
}