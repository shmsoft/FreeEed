/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author mark
 */
public class GsonUserTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(GsonUserTest.class);
    @Test
    public void testGsonUser() {
        GsonUser.main(null);
        assert (true);
    }
    @Test
    public void testGetValueByKey() {
        String jsonStr = "{\n\"summary\":\"Just some text\"\n}";
        String key = "summary";
        String value = new GsonUser().getValueByKey(jsonStr, key);
        assertEquals("Just some text", value);
    }
}