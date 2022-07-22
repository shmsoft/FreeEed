package org.freeeed.ai;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

class Student {
    public String name;
    public int rollNo;

    private Map<String, String> properties;
    public Student(){
        properties = new HashMap<>();
    }
    @JsonAnyGetter
    public Map<String, String> getProperties(){
        return properties;
    }
    public void add(String property, String value){
        properties.put(property, value);
    }
    @JsonCreator
    public Student(@JsonProperty("theName") String name, @JsonProperty("id") int rollNo){
        this.name = name;
        this.rollNo = rollNo;
    }
}