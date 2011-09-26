/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.main;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import org.freeeed.main.PlatformUtil.PLATFORM;

/**
 *
 * @author Mark
 */
public class PlatformUtilTest {

    public PlatformUtilTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testGetPlatform() {
        System.out.println("getPlatform");
        PLATFORM platform = PlatformUtil.getPlatform();
        System.out.println("Platform = " + platform);
    }
}
