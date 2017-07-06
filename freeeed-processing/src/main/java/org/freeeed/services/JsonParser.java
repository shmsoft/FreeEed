/*
 *
 * Copyright SHMsoft, Inc. 
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

import com.jayway.jsonpath.JsonPath;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/**
 *
 * @author mark
 */
public class JsonParser {

    public static String getJsonField(String jsonString, String fieldName) {
        return JsonPath.read(jsonString, "$." + fieldName);
    }

    public static Map <String, String> getJsonAsMap(String jsonLine) {
        JSONObject object = new JSONObject(jsonLine);
        String[] keys = JSONObject.getNames(object);
        Map<String, String> map = new HashMap<>();

        for (String key : keys) {
            Object value = object.get(key);
            // "id" is a special kind of field in SOLR
            // if it occurs in our doc - change it
            if (key.equalsIgnoreCase("id")) key = "json-id";
            // simple conversion to String            
            map.put(key, "" + value);            
        }
        return map;
    }
}
