package org.freeeed.services;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Review {

    public static boolean deliverFiles() throws IOException {
        Project project = Project.getProject();
        File outputFolder = new File(project.getResultsDir());        
        File[] files = outputFolder.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        // TODO find a way to see that MR job is running and results are not ready yet
        
        // if I have a "part...." file there, rename it to output.csv
        for (File file : files) {
            if (file.getName().startsWith("part")) {
                Files.move(file, new File(file.getParent() + "/metadata" + ParameterProcessing.METADATA_FILE_EXT));
            }
            if (file.getName().equals("_SUCCESS")) {
                file.delete();
            }
            
        }
        if (Stats.getInstance().getStatsFile().exists()) {
            Files.move(Stats.getInstance().getStatsFile(), new File(outputFolder.getPath() + "/report.txt"));
        }
        return true;
    }
}
