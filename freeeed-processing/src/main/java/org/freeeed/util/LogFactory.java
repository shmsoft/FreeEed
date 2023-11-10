package org.freeeed.util;

import java.util.logging.*;

public class LogFactory {
    // Obtain a logger instance from the Logger class
    private final static Logger LOGGER = LogFactory.getLogger(LogFactory.class.getName());

    public static void main(String[] args) {
        // Log messages with different importance levels
        LOGGER.severe("Severe Log");
        LOGGER.warning("Warning Log");
        LOGGER.info("Info Log");
        LOGGER.config("Config Log");
        LOGGER.fine("Fine Log");
        LOGGER.finer("Finer Log");
        LOGGER.finest("Finest Log");
    }
    public static Logger getLogger(String className) {
        Logger myLogger = Logger.getLogger(className);
        myLogger.setLevel(Level.ALL);
        myLogger.setUseParentHandlers(false); // Disable parent handlers
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        myLogger.addHandler(consoleHandler);
        try {
            FileHandler fileHandler = new FileHandler("logs/freeeed.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            myLogger.addHandler(fileHandler);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return myLogger;
    }
}
