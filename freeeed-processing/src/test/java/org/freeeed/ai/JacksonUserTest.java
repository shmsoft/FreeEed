/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 *
 * @author mark
 */
public class JacksonUserTest {
    @Test
    public void whenSerializingUsingJsonAnyGetter_thenCorrect()
            throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try{
            Student student = new Student();
            student.add("attr1", "value1");
            student.add("attr2", "value2");
            String jsonString = mapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(student);
            System.out.println(jsonString);
            assertThat(jsonString, containsString("attr1"));
            assertThat(jsonString, containsString("attr2"));
            assertThat(jsonString, containsString("value1"));
            assertThat(jsonString, containsString("value2"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void whenDeserializingUsingJsonCreate_thenCorrect()
            throws JsonProcessingException {
        String json = "{\"id\":1,\"theName\":\"Mark\"}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            Student student = mapper
                    .readerFor(Student.class)
                    .readValue(json);
            System.out.println(student.rollNo +", " + student.name);
            assertEquals(student.rollNo, 1);
            assertEquals(student.name, "Mark");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}