/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai.inabia;

import org.freeeed.services.Project;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author mark
 */
public class PiiTest {
    @Test 
    public void testGetPii() {
        System.out.println("testGetPii");
        String str = "Hello 713-777-7777 Name: John Doe, johndoe@gmail.com. Lorem Ipsum is simply dummy text of the printing and typesetting industry. 1301 McKinney St #2400, Houston, TX 77010";


        String str2 = "This is how I tried to split a paragraph into a sentence. But, there is a problem. My paragraph includes dates like Jan.13, 2014 , words like U.S and numbers like 2.2. They all got split by the above code. we might have a credit card number like: 6219-8610-2502-0511 or a cc number like: 6219861025020511. another possibility is a word with SSN in it likeL:  859-98-0987 or like 859980987. now, another type is CC number is possible: 6219 8610 2502 0511";

        InabiaClient a = new InabiaClient(str,"SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",100);

        try {
            System.out.println(a.getPII());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(true);
    }
}