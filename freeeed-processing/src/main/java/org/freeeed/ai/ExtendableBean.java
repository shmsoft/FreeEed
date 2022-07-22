package org.freeeed.ai;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

public class ExtendableBean {
    public String name;
    private Map<String, String> properties;
    ExtendableBean (String name) {
        this.name = name;
        properties = new java.util.HashMap<String, String>();
    }
    @JsonAnyGetter
    public void add(String key, String value) {
        properties.put(key, value);
    }
}