package org.freeeed.services;

import java.util.Properties;

/**
 *
 * @author mark
 */
public class Settings extends Properties {
    private static Settings settings;
    
    static public Settings getSettings() {
        return settings;
    }
    static public void setSettings(Settings aSettings) {
        settings = aSettings;
    }
}
