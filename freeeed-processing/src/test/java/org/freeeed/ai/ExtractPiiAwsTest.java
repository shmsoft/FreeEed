/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * @author mark
 * For testing
 * aws comprehend detect-pii-entities \
 * --text "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010" \
 * --language-code en
 */
public class ExtractPiiAwsTest {
    private String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";

    private String awsAccessKeyId = "";
    private String awsSecretAccessKey = "";
    private Region awsRegion = Region.US_EAST_1;

    @Before

    public void initKeys() {
        awsAccessKeyId = System.getenv("aws_access_key_id");
        awsSecretAccessKey = System.getenv("aws_secret_access_key");
    }

    @Test
    public void testAwsPii() {


        System.out.println("ExtractPiiAwsTest");
        String data = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";
        ExtractPiiAws extractor = new ExtractPiiAws(awsAccessKeyId, awsSecretAccessKey, awsRegion);
        HashMap pii = extractor.extractPII(data);
        assertTrue(pii.size() > 0);
    }
}