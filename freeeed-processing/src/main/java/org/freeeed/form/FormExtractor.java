package org.freeeed.form;

public class FormExtractor {

    public static void main(String[] args) {
        FormExtractor instance = new FormExtractor();
        instance.extract("Call me Ishmael");
    }
    public void extract(String text) {
        ArrestDataExtractor arrestDataExtractor = new ArrestDataExtractor();
        arrestDataExtractor.extract(text);
    }
}
