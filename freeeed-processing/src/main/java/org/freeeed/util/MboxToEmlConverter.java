package org.freeeed.util;/*
 *@created 06/07/2024- 21:04
 *@author neha
 */
import org.freeeed.services.Project;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MboxToEmlConverter {

    public static List<String> convertMboxToEml(String mboxFilePath, String mboxFileName, String outputDirPath) throws IOException {


        List<String> extractedFilePaths = new LinkedList<>();
        File mboxFile = new File(mboxFilePath);
        if (!mboxFile.exists() || !mboxFile.isFile()) {
            throw new FileNotFoundException("Invalid MBOX file path provided.");
        }

        Path outputDir = Paths.get(outputDirPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mboxFile))) {
            String curLine = reader.readLine();
            int mailItem = 0;

            while (curLine != null) {
                if (curLine.startsWith("From ")) {
                    Queue<String> multiline = new LinkedList<>();
                    String mboxName = mboxFileName.contains("/") ? mboxFileName.substring(mboxFileName.lastIndexOf("/")) : mboxFileName;
                    File emlFile = new File(outputDir.toFile(), mboxName+"_email_" + mailItem + ".eml");

                    try (BufferedWriter emlWriter = new BufferedWriter(new FileWriter(emlFile))) {
                        do {
                            if (!curLine.startsWith(" ") && !curLine.startsWith("\t")) {
                                multiline.add(curLine);
                            } else {
                                String latestLine = multiline.poll();
                                latestLine = latestLine + " " + curLine.trim();
                                multiline.add(latestLine);
                            }

                            emlWriter.write(curLine);
                            emlWriter.newLine();
                            curLine = reader.readLine();
                        } while (curLine != null && !curLine.startsWith("From "));

                        System.out.println("Email saved to: " + emlFile.getAbsolutePath());
                        extractedFilePaths.add(emlFile.getAbsolutePath());
                    }

                    mailItem++;
                } else {
                    curLine = reader.readLine();
                }
            }
        }
        return extractedFilePaths;
    }
}
