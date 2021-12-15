package org.freeeed.ai.inabia;

import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.List;

public class DocumentToSentences {

    private final String document;
    private final int maxLength;

    public DocumentToSentences(String document, int maxLength) {
        this.document = document;
        this.maxLength = maxLength;
    }

    public DocumentToSentences(String document) {
        this.document = document;
        this.maxLength = 50;
    }

    public List<String> getSentences() {
        String wrappedText = WordUtils.wrap(document, maxLength, "\n", true);
        return Arrays.asList(wrappedText.split("\n"));
    }

}
