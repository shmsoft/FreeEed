/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.transcribe;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class RestApiTranscribeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiTranscribeTest.class);


    //@Test
    public void testTranscriptionFromUrl() throws Exception {
        String url = "https://bit.ly/3yxKEIY";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.getTranscriptionFromUrl(url);
        System.out.println(transcript);
        assertTrue(true);
    }
    @Test
    public void testTranscribeDirectly() throws Exception {
        String filename = "test-data/06-audio/7510.mp3";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.transcribeDirectly(filename);
        System.out.println(transcript);
        assertTrue(true);
    }
}