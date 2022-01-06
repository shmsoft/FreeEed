package org.freeeed.ai;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

public class SentenceSplittingExample {
    public static String text = "Hello world. Hello world again.";
    public static void main(String[] args) {
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
            System.out.println(sent.text());
        }
    }
}

