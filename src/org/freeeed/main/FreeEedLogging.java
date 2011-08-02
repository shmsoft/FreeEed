package org.freeeed.main;

import java.io.File;

public class FreeEedLogging {
    public static final String logDir = "logs";
	public static final String history = logDir + "/" + "processing_history.txt";
	public static void init() {
		new File(logDir).mkdirs();
	}
}
