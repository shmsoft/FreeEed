/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ai;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author mark
 */
public class SummarizeTextTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeTextTest.class);

    private final String data = "House Speaker Nancy Pelosi and Democratic leaders have greenlighted a plan to craft legislation that would prohibit members of Congress from trading stock, after months of resistance to a ban by Pelosi, CNBC confirmed Wednesday. At Pelosi's direction, the House Administration Committee is working on drafting the rules, and the legislation is expected to be put up for a vote this year, likely before the November midterm elections.";
    private final String summary = "Members of Congress could soon be banned from trading on the stock market.";

    // TODO Put the test back in
    //@Test
    public void testSummarizeText() {
        SummarizeText summarizer = new SummarizeText();
        String createdSummary = summarizer.summarizeText(data);
        assertEquals(summary, createdSummary);
    }
    // TODO Put the test back in
    //@Test
    public void testSummarizeTextModel() {
        SummarizeText summarizer = new SummarizeText();
        String modelCodeName = "google/pegasus-xsum";
        String createdSummary = summarizer.summarizeText(data, modelCodeName);
        assertEquals(summary, createdSummary);
    }
}