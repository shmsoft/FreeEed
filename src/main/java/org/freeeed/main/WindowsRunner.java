package org.freeeed.main;

import com.google.common.io.Files;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.freeeed.services.History;
import org.freeeed.services.Project;

public class WindowsRunner {

    public static void run(String[] args) {
        try {
            Project project = Project.getProject();
            List<String> zipFiles = Files.readLines(
                    new File(project.getInventoryFileName()), 
                    Charset.defaultCharset());
            for (String zipFile : zipFiles) {
                History.appendToHistory("Processing: " + zipFile);

                // process archive file
                ZipFileProcessor processor = new ZipFileProcessor(zipFile, null);
                processor.process();
            }
            WindowsReduce.getInstance().cleanup(null);
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
