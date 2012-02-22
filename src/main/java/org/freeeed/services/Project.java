package org.freeeed.services;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Properties;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 */
public class Project extends Properties {

    public static Project project;
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

    public void generateProjectCode() {
        if (containsKey(ParameterProcessing.PROJECT_CODE)) {
            // do nothing, we have the code already
            return;
        }

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
}
