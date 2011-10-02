package org.freeeed.main;

import com.google.common.io.Files;
import de.schlichtherle.io.File;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.freeeed.services.History;

public class WindowsRunner {

    public static void run(String[] args) {
        try {
            Configuration config = FreeEedMain.getInstance().getProcessingParameters();
            List<String> zipFiles = Files.readLines(
                    new File(ParameterProcessing.inventoryFileName),
                    Charset.defaultCharset());
            for (String zipFile : zipFiles) {
                History.appendToHistory("Processing: " + zipFile);

                // process archive file
                ZipFileProcessor processor = new ZipFileProcessor(zipFile, null);
                processor.process();
            }
            WindowsReduce.getInstance().cleanup(null);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
