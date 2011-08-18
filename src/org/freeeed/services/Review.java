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

	public static void deliverFiles() throws IOException, FreeEedException {
		File outputFolder = new File(ParameterProcessing.OUTPUT_DIR + "/output");
		// if I have a "part...." file there, rename it to output.csv
		File[] files = outputFolder.listFiles();
		if (files == null) {
			throw new FreeEedException("No results yet");
		}
		boolean success = false;
		for (File file : files) {
			if (file.getName().startsWith("_SUCCESS")) {
				success = true;
			}
		}
		if (!success) {
			throw new FreeEedException("No results yet");
		}
		for (File file : files) {
			if (file.getName().startsWith("part")) {
				Files.move(file, new File(outputFolder.getPath() + "/metadata.csv"));
			}
		}
		if (Stats.getInstance().getStatsFile().exists()) {
			Files.move(Stats.getInstance().getStatsFile(), new File(outputFolder.getPath() + "/report.txt"));
		}
	}
}
