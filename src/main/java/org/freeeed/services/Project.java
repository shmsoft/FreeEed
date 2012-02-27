package org.freeeed.services;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private String run = "";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
    private int skip;
    private int docCount;
    private static String ENV_HADOOP = "hadoop";
    private static String ENV_LOCAL = "local";
    private static String ENV_EC2 = "ec2";
    private static String FS_HDFS = "hfds";
    private static String FS_S3 = "s3";
    private static String FS_LOCAL = "local";    

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
        settings.setLastProjectCode(projectCode);
        settings.save();
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
        project.parseSkip();
        return project;
    }

    private void parseSkip() {
        skip = Integer.parseInt(project.getProperty(ParameterProcessing.SKIP));
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
        project.parseSkip();
        return project;
    }

    public void save() {
        String projectFilePath = project.getProjectFilePath();
        try {
            store(new FileWriter(projectFilePath), "FreeEed Project");
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
        if (inputs != null && !inputs.trim().isEmpty()) {
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
        if (custodians != null && !custodians.trim().isEmpty()) {
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

    public String getStagingDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator + getRun() + "staging";
        return dir;
    }

    public String getRun() {
        return run;
    }

    public void setRun() {
        run = "run_" + dateFormat.format(new Date()) + File.separator;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getInventoryFileName() {
        String dir = getStagingDir() + File.separator
                + getProjectCode() + File.separator
                + "inventory";
        return dir;
    }

    public String getOuputDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator
                + getProjectCode() + File.separator;
        return dir;
    }

    public String getResultsDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator
                + getProjectCode() + File.separator
                + getRun() + "output";
        return dir;
    }

    public String getResultsOfMultipleRunsDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator + getRun() + "output";
        return dir;
    }

    public boolean isStage() {
        return getProperty(ParameterProcessing.STAGE) != null;
    }

    public String getProcessWhere() {
        return getProperty(ParameterProcessing.PROCESS_WHERE);
    }

    public int getFilesPerArchive() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.FILES_PER_ZIP_STAGING));
        } catch (Exception e) {
            return 50;
        }
    }

    public boolean isCreatePDF() {
        return getProperty(ParameterProcessing.CREATE_PDF) != null;
    }

    public boolean checkSkip() {
        boolean toSkip = false;
        if (skip > 0) {
            ++docCount;
            toSkip = (docCount > 1);
            if (docCount == skip + 1) {
                docCount = 0;
            }
            return toSkip;
        }
        return toSkip;
    }

    public boolean isEnvHadoop() {
        return ENV_HADOOP.equalsIgnoreCase(
                getProperty(ParameterProcessing.PROCESS_WHERE));
    }
    public boolean isEnvLocal() {
        return ENV_LOCAL.equalsIgnoreCase(
                getProperty(ParameterProcessing.PROCESS_WHERE));
    }
    public boolean isEnvEC2() {
        return ENV_EC2.equalsIgnoreCase(
                getProperty(ParameterProcessing.PROCESS_WHERE));
    }    
        public boolean isFsHdfs() {
        return FS_HDFS.equalsIgnoreCase(
                getProperty(ParameterProcessing.FILE_SYSTEM));
    }
    public boolean isFsLocal() {
        return FS_LOCAL.equalsIgnoreCase(
                getProperty(ParameterProcessing.FILE_SYSTEM));
    }
    public boolean isFsS3() {
        return FS_S3.equalsIgnoreCase(
                getProperty(ParameterProcessing.FILE_SYSTEM));
    }    
    public boolean isHadoopDebug() {
        return getProperty(ParameterProcessing.HADOOP_DEBUG) != null;
    }

}
