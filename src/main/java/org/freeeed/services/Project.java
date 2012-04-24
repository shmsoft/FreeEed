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
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
    private int skip;
    private int docCount;
    private static String ENV_HADOOP = "hadoop";
    private static String ENV_LOCAL = "local";
    private static String ENV_EC2 = "ec2";
    private static String FS_HDFS = "hdfs";
    private static String FS_S3 = "s3";
    private static String FS_LOCAL = "local";
    private static String OUTPUT = "output";
    private static String STAGING = "staging";
    private static String INVENTORY = "inventory";
    private static String RESULTS = "results";
    private String currentCustodian;

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
        int code = 1000;
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

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
        setProperty(ParameterProcessing.SKIP, "" + skip);
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
        // remove the run even if it was in the file        
        project.setRun("");
        return project;
    }

    public void save() {
        String projectFilePath = project.getProjectFilePath();
        try {
            store(new FileWriter(projectFilePath), ParameterProcessing.APP_NAME + " Project");
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
            builder.append(cull + ParameterProcessing.NL);
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
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator
                + getProjectCode() + File.separator
                + OUTPUT + File.separator
                + getRun()
                + STAGING;
        return dir;
    }

    public String getRun() {
        String run = getProperty(ParameterProcessing.RUN);
        if (run == null) {
            run = "";
        }
        return run;
    }

    public void setRun() {
        setProperty(ParameterProcessing.RUN, "run-" + dateFormat.format(new Date()) + File.separator);
    }

    public void setRun(String run) {
        setProperty(ParameterProcessing.RUN, run);
    }

    public String getInventoryFileName() {
        String dir = getStagingDir() + File.separator + INVENTORY;
        return dir;
    }

    public String getOutputDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator
                + getProjectCode() + File.separator
                + OUTPUT + File.separator
                + getRun();
        return dir;
    }

    public String getLoadFile() {
        return getResultsDir() + "/metadata" + ParameterProcessing.METADATA_FILE_EXT;
    }
        
    public String getResultsDir() {
        String dir = getOutputDir() + RESULTS;
        return dir;
    }

    public String getRunsDir() {
        String dir = ParameterProcessing.OUTPUT_DIR + File.separator
                + getProjectCode() + File.separator
                + OUTPUT;
        return dir;
    }

    public boolean isStage() {
        return getProperty(ParameterProcessing.STAGE) != null;
    }

    public String getProcessWhere() {
        return getProperty(ParameterProcessing.PROCESS_WHERE);
    }

    public double getGigsPerArchive() {
        try {
            return Double.parseDouble(getProperty(ParameterProcessing.GIGS_PER_ZIP_STAGING));
        } catch (Exception e) {
            return 1;
        }
    }
    
    public void setGigsPerArchive(double d) {
        setProperty(ParameterProcessing.GIGS_PER_ZIP_STAGING, Double.toString(d));
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

    public boolean isRemoveSystemFiles() {
        return getProperty(ParameterProcessing.REMOVE_SYSTEM_FILES) != null;
    }

    public void setRemoveSystemFiles(boolean b) {
        if (b) {
            setProperty(ParameterProcessing.REMOVE_SYSTEM_FILES, "");
        } else {
            remove(ParameterProcessing.REMOVE_SYSTEM_FILES);
        }

    }

    /**
     *
     * @return One of the predefined separators, or the actual value string
     */
    public String getFieldSeparator() {
        return getProperty(ParameterProcessing.FIELD_SEPARATOR);
    }

    public void setFieldSeparator(String fieldSeparator) {
        setProperty(ParameterProcessing.FIELD_SEPARATOR, fieldSeparator);
    }

    public boolean isMetadataCollectStandard() {
        return "standard".equalsIgnoreCase(getMetadataCollect());
    }
    public String getMetadataCollect() {
        return getProperty(ParameterProcessing.METADATA_COLLECTION);
    }

    public void setMetadataCollect(String metadataCollect) {
        setProperty(ParameterProcessing.METADATA_COLLECTION, metadataCollect);
    }

    public boolean isTextInMetadata() {
        return getProperty(ParameterProcessing.TEXT_IN_METADATA) != null;
    }
    
    public void setTextInMetadata(boolean b) {
        if (b) {
            setProperty(ParameterProcessing.TEXT_IN_METADATA, "");
        } else {
            remove(ParameterProcessing.TEXT_IN_METADATA);
        }
    }    

    public void setupCurrentCustodianFromFilename(String currentCustodian) {
        int underscore = currentCustodian.indexOf("_");
        if (underscore >= 0 && underscore + 1 < currentCustodian.length()) {
            currentCustodian = currentCustodian.substring(underscore + 1, currentCustodian.length() - 4);
            currentCustodian = currentCustodian.replaceAll("_", "_");
        } else {
            currentCustodian = "";
        }
        this.currentCustodian = currentCustodian;
    }
    public void setCurrentCustodian(String currentCustodian) {
        this.currentCustodian = currentCustodian.trim();
    }
    public String getCurrentCustodian() {
        return currentCustodian;
    }
    public String getFormattedCustodian() {
        return "_" + currentCustodian.replaceAll(" ", "_");
    }
}
