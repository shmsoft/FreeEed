/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai.inabia;

import org.freeeed.services.Project;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 *
 * @author mark
 */
public class PiiTest {
    @Test 
    public void testGetPii() {
        System.out.println("testGetPii");
        String str = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";

        InabiaClient client = new InabiaClient(str,"SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",100);

        try {
            String expectedResults = "{Address=1301 Mckinney St #2400, Houston, Tx 77010, Phone=713-777-7777, Name=John Doe}";
            String returnResult = client.getPII().toString();
            assertEquals(expectedResults, returnResult);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}