package org.freeeed.ai;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonUser {
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        Foo foo = new Foo(1, "foo");
        try {
            String jsonStr = mapper.writeValueAsString(foo);
            Foo fooBack = mapper.readValue(jsonStr, Foo.class);
            System.out.println(foo.getId());
            System.out.println(foo.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
