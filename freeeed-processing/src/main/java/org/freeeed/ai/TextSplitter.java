package org.freeeed.ai;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * For splitting texts into shorter section.
 * Currently, AWS and Inabia allow submitting text segments no longer than 5,000 characters.
 * This class splits the text into shorter sections observing this limitation.
 * We are using Stanford OpenNLP, splitting on sentences.
 */
public class TextSplitter {
    private int charLimit = 5000;
    TextSplitter(int charLimit) {
        this.charLimit = charLimit;
    }
    public List<String> splitBySentenceWithLimit(String text) {
        List<String> myList = splitBySentence(text);
        List<String> largerSegments = new ArrayList<>();
        StringBuilder largerSegment = new StringBuilder();
        for (int i = 0; i < myList.size(); ++i) {
            String fragment = myList.get(i);
            if (fragment.length() + largerSegment.length() < charLimit) {
                largerSegment.append(largerSegment.length() > 0 ? " " : "").append(fragment);
            } else {
                largerSegments.add(largerSegment.toString());
                largerSegment.setLength(0);
            }
        }
        if (largerSegment.length() > 0) {
            largerSegments.add(largerSegment.toString());
        }
        return largerSegments;
    }
    public List<String> splitBySentence(String text) {
        List<String> myList = new ArrayList<>();
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit");
        // build pipeline
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        // create a document object
        CoreDocument doc = new CoreDocument(text);
        // annotate
        pipeline.annotate(doc);
        // display sentences
        for (CoreSentence sent : doc.sentences()) {
            myList.add(sent.text());
        }
        return myList;
    }

}
