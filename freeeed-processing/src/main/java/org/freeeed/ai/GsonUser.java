package org.freeeed.ai;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class GsonUser {
    public static void main(String[] args) {

        // Serialization
        Gson gson = new Gson();
        String str = gson.toJson(1);


        // Deserialization
        int one = gson.fromJson("1", int.class);
        System.out.println(one);
        Integer two = gson.fromJson("2", Integer.class);
        System.out.println(two);
        Long three = gson.fromJson("3", Long.class);
        System.out.println(three);
        Boolean aFalse = gson.fromJson("false", Boolean.class);
        System.out.println(aFalse);
        String abc = gson.fromJson("\"abc\"", String.class);
        System.out.println(abc);
        String[] anotherStr = gson.fromJson("[\"abc\"]", String[].class);
        System.out.println(Arrays.toString(anotherStr));
    }
    public String getValueByKey(String jsonStr, String key) {
        String summaryMarker = "\"summary\":";
        int summaryStart = jsonStr.indexOf(summaryMarker) + summaryMarker.length() + 1;
        int summaryEnd = jsonStr.indexOf("\n", summaryStart);
        return jsonStr.substring(summaryStart, summaryEnd - 1);
    }
}
