package org.freeeed.main;

import java.util.HashMap;
import java.util.Set;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * Flatten only the String values into a HashMap
 *
 * @author mark
 */
public class TestUtil {

    public static HashMap<String, String> flatten(MapWritable map) {
        HashMap<String, String> flat = new HashMap<>();
        Set<Writable> keySet = map.keySet();
        for (Writable key : keySet) {
            Writable value = map.get(key);
            if (key instanceof Text && value instanceof Text) {
                flat.put(((Text) key).toString(), ((Text) value).toString());
            }
        }
        return flat;
    }
}
