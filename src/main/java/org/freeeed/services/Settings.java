package org.freeeed.services;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Settings extends Properties {

    private static final String SETTINGS = "settings.properties";
    private static Settings settings = new Settings();

    static public Settings getSettings() {
        return settings;
    }

    static public void setSettings(Settings aSettings) {
        settings = aSettings;
    }

    public String getLastProjectCode() {
        return getProperty(ParameterProcessing.LAST_PROJECT_CODE);
    }

    public void setLastProjectCode(String projectCode) {
        setProperty(ParameterProcessing.LAST_PROJECT_CODE, projectCode);
    }

    public static void load() {
        if (!new File(SETTINGS).exists()) {
            settings.setLastProjectCode("1000");
            return;
        }
        try {
            settings.load(new FileReader(SETTINGS));
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    public void save() {
        try {
            settings.store(new FileWriter(SETTINGS), "FreeEed Settings");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
    public String getCurrentDir() {
        return getProperty(ParameterProcessing.CURRENT_DIR);
    }
    public void setCurrentDir(String filePath) {
        setProperty(ParameterProcessing.CURRENT_DIR, filePath);
    }
}
