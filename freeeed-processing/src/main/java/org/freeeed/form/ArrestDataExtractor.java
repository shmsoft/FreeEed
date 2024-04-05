package org.freeeed.form;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrestDataExtractor {

    public static void main(String[] args) {
        ArrestDataExtractor instance = new ArrestDataExtractor();
        instance.extract();
    }

    public void extract() {
        String text = "ARREST DATE: 07/28/2000 AGENCY: NEVADA DMVPS PAROLE AND PROBATION";

        // Patterns to match the arrest date and agency
        String datePattern = "ARREST DATE: (\\d{2}/\\d{2}/\\d{4})";
        String agencyPattern = "AGENCY: (.+)$";

        // Compile patterns
        Pattern datePat = Pattern.compile(datePattern);
        Pattern agencyPat = Pattern.compile(agencyPattern);

        // Create matchers
        Matcher dateMatcher = datePat.matcher(text);
        Matcher agencyMatcher = agencyPat.matcher(text);

        if (dateMatcher.find()) {
            System.out.println("Arrest Date: " + dateMatcher.group(1));
        }

        if (agencyMatcher.find()) {
            System.out.println("Agency: " + agencyMatcher.group(1));
        }

    }
}