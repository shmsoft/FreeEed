package org.freeeed.form;

public class FormExtractor {

    public static void main(String[] args) {
        FormExtractor instance = new FormExtractor();
        instance.extract();
    }
    public void extract() {
        ArrestDataExtractor arrestDataExtractor = new ArrestDataExtractor();
        arrestDataExtractor.extract();
    }
}
