package org.freeeed.services;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Settings extends Properties {

    private static final String SETTINGS = "settings.properties";
    private static Settings settings = new Settings();
    private final static int MAX_RECENT_PROJECTS = 8;

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
            settings.store(new FileWriter(SETTINGS), ParameterProcessing.APP_NAME + " Settings");
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

    public List<Properties> getRecentProjects() {
        ArrayList<Properties> recentProjects = new ArrayList<Properties>();
        String recentProjectsStr = getProperty(ParameterProcessing.RECENT_PROJECTS);
        if (recentProjectsStr == null) {
            return recentProjects;
        }
        String[] projects = recentProjectsStr.split(",");
        for (String project : projects) {
            project = project.trim();
            if (new File(project).exists()) {
                try {
                    Properties pr = new Properties();
                    pr.load(new FileInputStream(project));
                    recentProjects.add(pr);
                } catch (Exception e) {
                    History.appendToHistory("Warning: project " + project);
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Properties project : recentProjects) {
            builder.append(project.getProperty(ParameterProcessing.PROJECT_FILE_PATH));
        }
        save();
        return recentProjects;
    }

    public void addRecentProject(String recentProjectPath) {
        List<Properties> projects = getRecentProjects();
        for (Properties project : projects) {
            if (recentProjectPath.equalsIgnoreCase(
                    project.getProperty(ParameterProcessing.PROJECT_FILE_PATH))) {
                return;
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(recentProjectPath).append(",");
        int nProj = 1;
        for (Properties project : projects) {
            ++nProj;
            if (nProj > MAX_RECENT_PROJECTS) {
                break;
            }
            builder.append(project.getProperty(ParameterProcessing.PROJECT_FILE_PATH)).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.RECENT_PROJECTS, builder.toString());
        save();
    }
    public boolean isUseJpst() {
        return getProperty(ParameterProcessing.USE_JPST) != null;
    }
}
