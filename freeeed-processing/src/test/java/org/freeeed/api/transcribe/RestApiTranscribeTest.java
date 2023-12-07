/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.api.transcribe;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class RestApiTranscribeTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestApiTranscribeTest.class);


    @Test
    public void testTranscriptionFromUrl() throws Exception {
        Date start = new Date();
        String url = "https://bit.ly/3yxKEIY";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.getTranscriptionFromUrl(url);
        System.out.println(transcript);
        assertTrue(true);
        Date end = new Date();
        System.out.println("testTranscriptionFromUrl time elapsed: " + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }
    @Test
    public void testTranscriptionFromFile() throws Exception {
        Date start = new Date();
        String filename = "test-data/06-audio/7510.mp3";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.getTranscriptionFromFile(filename);
        assertTrue(transcript.startsWith("You know, demons on TV like that"));
        Date end = new Date();
        System.out.println("testTranscriptionFromFile time elapsed: " + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }
    @Test
    public void testTranscriptionWithFastAPI() throws Exception {
        Date start = new Date();
        String filename = "test-data/06-audio/7510.mp3";
        RestApiTranscript restApiTranscript = new RestApiTranscript();
        String transcript = restApiTranscript.transcribeWithFastAPI(filename);
        assertTrue(transcript.contains("You know, demons on TV like that"));
        Date end = new Date();
        System.out.println("testTranscriptionFromFile time elapsed: " + (end.getTime() - start.getTime()) / 1000 + " seconds");
    }
}