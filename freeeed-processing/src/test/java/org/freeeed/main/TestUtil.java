package org.freeeed.main;

import java.util.HashMap;
import java.util.Set;

/**
 * Flatten only the String values into a HashMap
 *
 * @author mark
 */
public class TestUtil {

    public static HashMap<String, String> flatten(HashMap<String, String> map) {
        HashMap<String, String> flat = new HashMap<>();
        Set<String> keySet = map.keySet();
        for (String key : keySet) {
            String value = map.get(key);
            if (key instanceof String && value instanceof String) {
                flat.put(( key).toString(), (value).toString());
            }
        }
        return flat;
    }
}
