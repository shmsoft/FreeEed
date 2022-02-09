package org.freeeed.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testing in the browser: http://18.218.29.151:8000/docs#
 */

public class SummarizeText {
    private static final Logger LOGGER = LoggerFactory.getLogger(SummarizeText.class);
    public String summarizeText(String fullText) {
        String mtext = fullText.replaceAll("<br>", " ").trim();
        mtext = new AIUtil().removeBreakingCharacters(mtext);
        mtext = "{ \"text\":" + "\"" + mtext + "\"}";
        // TODO summarize!
        return mtext;
    }
}
