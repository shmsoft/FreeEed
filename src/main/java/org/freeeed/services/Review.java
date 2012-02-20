package org.freeeed.services;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.freeeed.main.FreeEedException;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Review {

    public static boolean deliverFiles() throws IOException, FreeEedException {
        File outputFolder = new File(ParameterProcessing.getResultsDir());        
        File[] files = outputFolder.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        // TODO find a way to see that MR job is running and results are not ready yet
        
        // if I have a "part...." file there, rename it to output.csv
        for (File file : files) {
            if (file.getName().startsWith("part")) {
                Files.move(file, new File(outputFolder.getPath() + "/metadata.csv"));
            }
        }
        if (Stats.getInstance().getStatsFile().exists()) {
            Files.move(Stats.getInstance().getStatsFile(), new File(outputFolder.getPath() + "/report.txt"));
        }
        return true;
    }
}
