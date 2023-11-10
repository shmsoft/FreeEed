package org.freeeed.util;

import java.util.logging.*;

public class LogFactory {
    // Obtain a logger instance from the Logger class
    private final static Logger LOGGER = getLogger(LogFactory.class.getName());

    public static void main(String[] args) {
        LOGGER.setLevel(Level.ALL);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);

        // Assign the ConsoleHandler to the Logger
        LOGGER.addHandler(consoleHandler);
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
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        try {
            FileHandler fileHandler = new FileHandler("logs/freeeed.log", true);
            // Set the formatter for the fileHandler
            fileHandler.setFormatter(new SimpleFormatter());
            myLogger.addHandler(fileHandler);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return myLogger;
    }
}
