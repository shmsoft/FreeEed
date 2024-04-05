package org.freeeed.form;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrestDataExtractor {

    public static void main(String[] args) {
        ArrestDataExtractor instance = new ArrestDataExtractor();
        instance.extract("Test tp extract arrest data");
    }

    public void extract(String text) {

        // Patterns to match the arrest date and agency
        String datePattern = "ARREST DATE: (\\d{2}/\\d{2}/\\d{4})";
        String agencyPattern = "AGENCY: (.*?) (?=ARREST DATE:|$)";

        // Compile patterns
        Pattern datePat = Pattern.compile(datePattern);
        Pattern agencyPat = Pattern.compile(agencyPattern);

        // Create matchers
        Matcher dateMatcher = datePat.matcher(text);
        Matcher agencyMatcher = agencyPat.matcher(text);

        // Find all matches for arrest dates
        System.out.println("Arrest Dates and Agencies:");
        while (dateMatcher.find()) {
            System.out.println("Arrest Date: " + dateMatcher.group(1));
            if (agencyMatcher.find()) {
                System.out.println("Agency: " + agencyMatcher.group(1));
            }
        }
    }
}
