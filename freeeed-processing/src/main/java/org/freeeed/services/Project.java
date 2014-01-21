/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Files;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeeed.ec2.S3Agent;
import org.freeeed.main.ParameterProcessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combine all project properties in one object. Pass this object around as a singleton, but also use it on the cluster
 * in MR processing. Use fluent interface http://en.wikipedia.org/wiki/Fluent_interface.
 *
 * @author mark
 */
public class Project extends Properties {

    private static final Logger logger = LoggerFactory.getLogger(Project.class);
    private static Project project = new Project();
    private final DecimalFormat projectCodeFormat = new DecimalFormat("0000");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd-HHmmss");
    //private int docCount;
    private static String ENV_HADOOP = "hadoop";
    public static String ENV_LOCAL = "local";
    private static String ENV_EC2 = "ec2";
    private static String FS_HDFS = "hdfs";
    private static String FS_S3 = "s3";
    private static String FS_LOCAL = "local";
    private static String OUTPUT = "output";
    private static String STAGING = "staging";
    private static String INVENTORY = "inventory";
    private static String RESULTS = "results";
    private String currentCustodian;
    private int mapItemStart = 1;
    private int mapItemEnd = 0;
    private int mapItemCurrent = 0;

    private Project() {
        // singleton
    }

    /**
     * Return the true or false for a specific property. All true properties in the Project setup are coded with either
     * property-key=yes. Anything else, such as key absent, value="no" or value = "false" results in false
     *
     * @param propertyKey the key we are checking
     * @return true if the property is present and its values is "true", and false otherwise
     */
    @VisibleForTesting
    boolean isPropertyTrue(String propertyKey) {
        String propertyValue = getProperty(propertyKey);
        if (propertyValue != null) {
            return Boolean.valueOf(propertyValue);
        } else {
            return false;
        }
    }

    /**
     * @return the mapItemStart
     */
    public int getMapItemStart() {
        return mapItemStart;
    }

    /**
     * @param mapItemStart the mapItemStart to set
     */
    public void setMapItemStart(int mapItemStart) {
        this.mapItemStart = mapItemStart;
    }

    /**
     * @return the mapItemEnd
     */
    public int getMapItemEnd() {
        return mapItemEnd;
    }

    /**
     * @param mapItemEnd the mapItemEnd to set
     */
    public void setMapItemEnd(int mapItemEnd) {
        this.mapItemEnd = mapItemEnd;
    }

    /**
     * @return the mapItemCurrent
     */
    public int getMapItemCurrent() {
        return mapItemCurrent;
    }

    /**
     * @param mapItemCurrent the mapItemCurrent to set
     */
    public void setMapItemCurrent(int mapItemCurrent) {
        this.mapItemCurrent = mapItemCurrent;
    }

    public enum DATA {

        LOCAL, URI, PROBLEM
    };

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
            logger.warn("Warning: problem parsing project, code = {}", projectCode);
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

    public static synchronized Project loadFromString(String str) {
        project = new Project();
        if (str == null) {
            return project;
        }
        try {
            project.load(new StringReader(str.substring(0, str.length() - 1).replace(", ", "\n")));
            HashMap<String, String> map2 = new HashMap<>();
            for (java.util.Map.Entry<Object, Object> e : project.entrySet()) {
                map2.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return project;
    }

    public static synchronized Project loadFromFile(File file) {
        project = new Project();
        try {
            project.load(new FileReader(file));
            project.setProjectFileName(file.getName());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return project;
    }

    public static synchronized Project loadStandaloneFromFile(File file) {
        Project standaloneProject = new Project();
        try {
            standaloneProject.load(new FileReader(file));
            standaloneProject.setProjectFileName(file.getName());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return standaloneProject;
    }

    
    public void save() {
        String projectFilePath = project.getProjectFilePath();
        if (projectFilePath == null) {
        }
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

    public String[] getCustodians(String[] inputs) {
        String custodians = getProperty(ParameterProcessing.PROJECT_CUSTODIANS);
        if (custodians != null && !custodians.trim().isEmpty()) {
            return custodians.split(",");
        } else {
            String[] custs = new String[inputs.length];
            for (int i = 0; i < custs.length; ++i) {
                custs[i] = "";
            }
            return custs;
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

    public Project setEnvironment(String environment) {
        setProperty(ParameterProcessing.PROCESS_WHERE, environment);
        return this;
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
        String dir = getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT + File.separator
                + getRun() + File.separator
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
        setProperty(ParameterProcessing.RUN, "run-" + dateFormat.format(new Date()));
    }

    public void setRun(String run) {
        setProperty(ParameterProcessing.RUN, run);
    }

    public String getInventoryFileName() {
        String dir = getStagingDir() + File.separator + INVENTORY;
        return dir;
    }

    public List<String> getInventory() throws IOException {
        DATA locationType = getDataLocationType();
        switch (locationType) {
            case LOCAL:
                return Files.readLines(
                        new File(getInventoryFileName()),
                        Charset.defaultCharset());
            case URI:
                return Arrays.asList(getInputs());
            case PROBLEM:
                return null;

        }
        return null;

    }

    public DATA getDataLocationType() {
        DATA locationType = null;
        String[] inputs = getInputs();
        for (String input : inputs) {
            if (new File(input).exists()) {
                locationType = DATA.LOCAL;
                break;
            } else if (true) {
                // TODO check for valid URI
                locationType = DATA.URI;
            } else {
                locationType = DATA.PROBLEM;
            }
        }
        // TODO right now, this is all one type, should we keep it that way?
        // if yes, add verification that this is indeed so
        return locationType;
    }

    public boolean hasStagedData() {
        DATA locationType = getDataLocationType();
        switch (locationType) {
            case LOCAL:
                if (!new File(getInventoryFileName()).exists()) {
                    return false;
                } else {
                    return true;
                }
            case URI:
                return true;
            case PROBLEM:
                return false;
            default:
                return false;
        }
    }

    public String getOut() {
        return Settings.getSettings().getOutputDir()
                + ParameterProcessing.OUTPUT_DIR;
    }

    public String getOutputDir() {
        String dir = getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT + File.separator
                + getRun();
        return dir;
    }

    public String getLoadFile() {
        return getResultsDir() + "/metadata" + ParameterProcessing.METADATA_FILE_EXT;
    }

    public String getResultsDir() {
        String dir = getOutputDir() + File.separator + RESULTS;
        return dir;
    }

    public String getRunsDir() {
        String dir = getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT;
        return dir;
    }

    public boolean isStage() {
        return isPropertyTrue(ParameterProcessing.STAGE);
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
        return isPropertyTrue(ParameterProcessing.CREATE_PDF);
    }

    public void setCreatePDF(boolean createPDF) {
        setProperty(ParameterProcessing.CREATE_PDF, Boolean.toString(createPDF));
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

    public boolean isRemoveSystemFiles() {
        return isPropertyTrue(ParameterProcessing.REMOVE_SYSTEM_FILES);
    }

    public void setRemoveSystemFiles(boolean b) {
        setProperty(ParameterProcessing.REMOVE_SYSTEM_FILES, Boolean.toString(b));
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
        return isPropertyTrue(ParameterProcessing.TEXT_IN_METADATA);
    }

    public void setTextInMetadata(boolean b) {
        if (b) {
            setProperty(ParameterProcessing.TEXT_IN_METADATA, "");
        } else {
            remove(ParameterProcessing.TEXT_IN_METADATA);
        }
    }

    public void setupCurrentCustodianFromFilename(String fileName) {
        try {
            fileName = URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.debug("Unable to decode file name {}", fileName);
        }
        
        List<String> patterns = getCustodianPatterns();
        for (String custodianPattern : patterns) {
            currentCustodian = "";
            
            String[] custodianPatternArr = custodianPattern.split("\\|\\|END\\|\\|");
            String custodianRegexp = custodianPatternArr[0];
            
            String custodianNamePattern = null;
            if (custodianPatternArr.length > 1) {
                custodianNamePattern = custodianPatternArr[1];
                currentCustodian = custodianPatternArr[1];
            }
            
            Pattern pattern = Pattern.compile(custodianRegexp);
            
            Matcher matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                for (int i = 1; i < matcher.groupCount() + 1; i ++) {
                    if (custodianNamePattern != null) {
                        currentCustodian = currentCustodian.replace("{" + i + "}", matcher.group(i));
                    } else {
                        currentCustodian += " " + matcher.group(i);
                    }
                }
                
                currentCustodian = currentCustodian.trim();
                break;
            }
        }
    }

    public Project setCurrentCustodian(String currentCustodian) {
        this.currentCustodian = currentCustodian.trim();
        return this;
    }

    public String getCurrentCustodian() {
        return currentCustodian;
    }

    public String getFormattedCustodian() {
        return "_" + currentCustodian.replaceAll(" ", "_");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Object[] keys = keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            builder.append(key.toString()).append("=").
                    append(get(key).toString()).append("\n");
        }
        return builder.toString();
    }

    public void setFileSystem(String fileSystem) {
        setProperty(ParameterProcessing.FILE_SYSTEM, fileSystem);
    }

    public Project cloneForS3() throws IOException {
        Project clone = (Project) clone();
        clone.setEnvironment(ENV_HADOOP);
        clone.setFileSystem(FS_S3);
        List<String> zipFiles = project.getInventory();
        String[] s3inputs = new String[zipFiles.size()];
        for (int i = 0; i < zipFiles.size(); ++i) {
            String input = zipFiles.get(i);
            // if the file exists, create the corresponding s3 path
            // but if it does not exist, assume that this is already a good URI
            if (new File(input).exists()) {
                String key = S3Agent.pathToKey(input);
                String s3key = "s3://" + Settings.getSettings().getProjectBucket() + "/" + key;
                s3inputs[i] = s3key;
            } else {
                s3inputs[i] = input;
            }
        }
        clone.setInputs(s3inputs);
        //remove the project input paths property as it is not needed on real processing
        clone.remove(ParameterProcessing.PROJECT_FILE_PATH);
        return clone;
    }

    public void resetCurrentMapCount() {
        mapItemCurrent = 0;
    }

    public void incrementCurrentMapCount() {
        ++mapItemCurrent;
    }

    public boolean isMapCountWithinRange() {
        if (mapItemEnd == 0) {
            return true;
        }
        if (mapItemEnd > 0) {
            return (mapItemCurrent >= mapItemStart && mapItemCurrent <= mapItemEnd);
        } else {
            return (mapItemCurrent >= mapItemStart);
        }
    }

    /**
     * Set the ocrEnabled parameter for this project. OCR processing is done only if the ocrEnabled parameter is set to
     * true.
     *
     * @param ocrEnabled
     */
    public void setOcrEnabled(boolean ocrEnabled) {
        setProperty(ParameterProcessing.OCR_ENABLED, Boolean.toString(ocrEnabled));
    }

    /**
     * Returns the value for the ocrEnabled parameter for this project.
     *
     * Returns true only if the ocrEnabled is set to "true".
     *
     * By default returns "false".
     *
     * @return
     */
    public boolean isOcrEnabled() {
        return isPropertyTrue(ParameterProcessing.OCR_ENABLED);
    }

    /**
     *
     * Set the value for Lucene FS index creation. If set to true, Lucene FS index will be created during the document
     * scan.
     *
     * @param luceneIndexEnabled
     */
    public void setLuceneIndexEnabled(boolean luceneIndexEnabled) {
        setProperty(ParameterProcessing.LUCENE_FS_INDEX_ENABLED, Boolean.toString(luceneIndexEnabled));
    }

    /**
     *
     * Return true if Lucene index creation is enabled.
     *
     * @return
     */
    public boolean isLuceneIndexEnabled() {
        return isPropertyTrue(ParameterProcessing.LUCENE_FS_INDEX_ENABLED);
    }

    /**
     * Set the if the send to solr is enabled.
     *
     * @param enabled
     */
    public void setSendIndexToSolrEnabled(boolean enabled) {
        setProperty(ParameterProcessing.SEND_INDEX_SOLR_ENABLED, Boolean.toString(enabled));
    }

    /**
     *
     * Return true if the Send index to Solr is selected.
     *
     * @return
     */
    public boolean isSendIndexToSolrEnabled() {
        return isPropertyTrue(ParameterProcessing.SEND_INDEX_SOLR_ENABLED);
    }

    /**
     * Set the if to add email attachments to generated PDFs
     *
     * @param enabled
     */
    public Project setAddEmailAttachmentToPDF(boolean enabled) {
        setProperty(ParameterProcessing.ADD_EMAIL_ATTACHMENT_TO_PDF, Boolean.toString(enabled));
        return this;
    }

    public boolean isAddEmailAttachmentToPDF() {
        return isPropertyTrue(ParameterProcessing.ADD_EMAIL_ATTACHMENT_TO_PDF);
    }

    public void setOcrMaxImagesPerPDF(int ocrMaxImages) {
        setProperty(ParameterProcessing.OCR_MAX_IMAGES_PER_PDF, "" + ocrMaxImages);
    }

    public int getOcrMaxImagesPerPDF() {
        String sendIndexToSolrEnabledStr = getProperty(ParameterProcessing.OCR_MAX_IMAGES_PER_PDF);
        if (sendIndexToSolrEnabledStr != null) {
            try {
                return Integer.parseInt(sendIndexToSolrEnabledStr);
            } catch (Exception e) {
            }
        }

        return 10;
    }

    public List<String> getCustodianPatterns() {
        List<String> result = new ArrayList<String>();
        
        String pattern = null;
        int count = 1;
        String key = ParameterProcessing.CUSTODIAN_PATTERN + count; 
        while ((pattern = getProperty(key)) != null) {
            result.add(pattern);
            key = ParameterProcessing.CUSTODIAN_PATTERN + (++count);
        }
        
        return result;
    }
    
    /**
     * Remove all settings from project.
     */
    public static Project setEmptyProject() {
        project = new Project();
        return project;
    }
}
