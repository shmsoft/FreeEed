package org.freeeed.services;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Properties;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Project extends Properties {

    private static Project project = new Project();
    private final DecimalFormat projectCodeFormat = new DecimalFormat("0000");

    public String getBucket() {
        return getProperty(ParameterProcessing.S3BUCKET);
    }

    public void setBucket(String bucket) {
        setProperty(ParameterProcessing.S3BUCKET, bucket);
    }

    public String getProjectCode() {
        return getProperty(ParameterProcessing.PROJECT_CODE);
    }

    public String generateProjectCode() {
        if (containsKey(ParameterProcessing.PROJECT_CODE)) {
            // do nothing, we have the code already
            return getProperty(ParameterProcessing.PROJECT_CODE);
        }
        Settings settings = Settings.getSettings();
        String projectCode = settings.getLastProjectCode();
        int code = 0;
        try {
            code = Integer.parseInt(projectCode);
        } catch (NumberFormatException e) {
            History.appendToHistory("Warning: problem parsing project code " + projectCode);
        }
        ++code;
        projectCode = projectCodeFormat.format(code);
        setProperty(ParameterProcessing.PROJECT_CODE, projectCode);
        return projectCode;
    }

    public static Project getProject() {
        return project;
    }

    public static void setProject(Project aProject) {
        project = aProject;
    }

    public static Project loadFromString(String str) {
        Project proj = new Project();
        if (str == null) {
            return project;
        }
        try {
            proj.load(new StringReader(str.substring(1, str.length() - 1).replace(", ", "\n")));
            HashMap<String, String> map2 = new HashMap<String, String>();
            for (java.util.Map.Entry<Object, Object> e : proj.entrySet()) {
                map2.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        project = proj;
        return project;
    }

    public static Project loadFromFile(File file) {
        try {
            Project proj = new Project();
            proj.load(new FileReader(file));
            proj.setProjectFileName(file.getName());
            project = proj;
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return project;
    }

    public void save() {
        try {
            getProject().store(new FileWriter(getProject().getProjectFileName()), "FreeEed Project");
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }

    }

    public void setProjectName(String projectName) {
        setProperty(ParameterProcessing.PROJECT_NAME, projectName);
    }

    public String getProjectName() {
        return getProperty(ParameterProcessing.PROJECT_NAME);
    }

    public String getNewProjectName() {
        return getProperty(ParameterProcessing.NEW_PROJECT_NAME);
    }

    public void setProjectFileName(String fileName) {
        setProperty(ParameterProcessing.PROJECT_FILE_NAME, fileName);
    }

    public String getProjectFileName() {
        return getProperty(ParameterProcessing.PROJECT_FILE_NAME);
    }

        public void setProjectFilePath(String filePath) {
        setProperty(ParameterProcessing.PROJECT_FILE_PATH, filePath);
    }

    public String getProjectFilePath() {
        return getProperty(ParameterProcessing.PROJECT_FILE_PATH);
    }

    public String[] getInputs() {
        String inputs = getProperty(ParameterProcessing.PROJECT_INPUTS);
        if (inputs != null) {
            return inputs.split(",");
        } else {
            return new String[0];
        }
    }

    public void setInputs(String[] inputs) {
        StringBuilder builder = new StringBuilder();
        for (String input : inputs) {
            builder.append(input).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.PROJECT_INPUTS, builder.toString());
    }

    public String[] getCustodians() {
        String custodians = getProperty(ParameterProcessing.PROJECT_CUSTODIANS);
        if (custodians != null) {
            return custodians.split(",");
        } else {
            return new String[0];
        }
    }

    public void setCustodians(String[] custodians) {
        StringBuilder builder = new StringBuilder();
        for (String custodian : custodians) {
            builder.append(custodian).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.PROJECT_CUSTODIANS, builder.toString());
    }

    public String getCullingAsTextBlock() {
        String culling = getProperty(ParameterProcessing.CULLING);
        if (culling == null) {
            return "";
        }
        String culls[] = culling.split(",");
        StringBuilder builder = new StringBuilder();
        for (String cull : culls) {
            builder.append(cull + Util.NL);
        }
        return builder.toString();
    }

    public void setEnvironment(String environment) {
        setProperty(ParameterProcessing.PROCESS_WHERE, environment);
    }

    public void setCulling(String cullingStr) {
        StringBuilder builder = new StringBuilder();
        String[] cullings = cullingStr.split("\n");
        for (String culling : cullings) {
            culling = culling.replaceAll(",", "");
            builder.append(culling).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.CULLING, builder.toString());
    }
}
