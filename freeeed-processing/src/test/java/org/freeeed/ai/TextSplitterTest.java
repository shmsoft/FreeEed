/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import org.junit.Before;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author mark
 */
public class TextSplitterTest {
    public static String text = "Hello world. Hello world again.";
    private int LENGTH_LIMIT = 5000;
    @Test
    public void testSplitBySentence() {
        TextSplitter splitter = new TextSplitter(LENGTH_LIMIT);
        List<String> sentences = splitter.splitBySentence(text);
        assertEquals(sentences.size(), 2);
    }
    @Test
    public void testSplitBySentenceWithLimit() {
        TextSplitter splitter = new TextSplitter(LENGTH_LIMIT);
        List<String> sentences = splitter.splitBySentenceWithLimit(text);
        assertEquals(sentences.size(), 1);
        List<String> sentences160 = splitter.splitBySentenceWithLimit(repeater(160));
        assertEquals(sentences160.size(), 2);
        assertTrue(sentences160.get(0).length() < LENGTH_LIMIT);
        assertTrue(sentences160.get(1).length() < LENGTH_LIMIT);
        List<String> sentences320 = splitter.splitBySentenceWithLimit(repeater(320));
        assertEquals(sentences320.size(), 3);
        assertTrue(sentences320.get(0).length() < LENGTH_LIMIT);
        assertTrue(sentences320.get(1).length() < LENGTH_LIMIT);
        assertTrue(sentences320.get(2).length() < LENGTH_LIMIT);

    }
    private String repeater(int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            builder.append(text).append(" ");
        }
        return builder.toString();
    }
}