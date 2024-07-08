package org.freeeed.util;

import junit.framework.TestCase;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Test;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

/*
 *@created 08/07/2024- 09:41
 *@author neha
 */public class MboxToEmlConverterTest {

    @Test
    public void testMboxToEmlConverter() {
        try {
            List<String> result = MboxToEmlConverter.convertMboxToEml("test-data/10-MBOX/simple.mbox", "/tmp/mboxfiles");
            assertNotNull(result);
            assertEquals(2, result.size());

            Path firstEml = Paths.get(result.get(0));
            assertTrue(Files.exists(firstEml));
            assertTrue(firstEml.toString().endsWith("_email_0.eml"));

            Path secondEml = Paths.get(result.get(1));
            assertTrue(Files.exists(secondEml));
            assertTrue(secondEml.toString().endsWith("_email_1.eml"));

        } catch (IOException e) {
            fail("IOException thrown during test: " + e.getMessage());
        }
    }
    @Test
    public void testHeadersInEmlFiles() {
        try {
            List<String> result = MboxToEmlConverter.convertMboxToEml("test-data/10-MBOX/headers.mbox", "/tmp/mboxfiles");
            assertNotNull(result);
            assertEquals(1, result.size());

            Path emlFilePath = Paths.get(result.get(0));
            List<String> lines = Files.readAllLines(emlFilePath);

            assertTrue(lines.stream().anyMatch(line -> line.startsWith("From envelope-sender-mailbox-name")));
            assertTrue(lines.stream().anyMatch(line -> line.startsWith("Return-Path: <name@domain.com>")));
            assertTrue(lines.stream().anyMatch(line -> line.startsWith("Subject: subject")));
            assertTrue(lines.stream().anyMatch(line -> line.startsWith("From: <author@domain.com>")));
            assertTrue(lines.stream().anyMatch(line -> line.startsWith("Date: Tue, 9 Jun 2009 23:58:45 -0400")));

            assertTrue(lines.stream().anyMatch(line -> line.contains("Test content")));

        } catch (IOException e) {
            fail("IOException thrown during test: " + e.getMessage());
        }
    }

}