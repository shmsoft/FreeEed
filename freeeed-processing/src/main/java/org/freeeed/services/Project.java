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
import org.freeeed.ai.SummarizeText;
import org.freeeed.ec2.S3Agent;
import org.freeeed.main.ParameterProcessing;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * Combine all project properties in one object. Contains reference to 'current
 * project.' Use fluent interface http://en.wikipedia.org/wiki/Fluent_interface.
 *
 * @author mark
 */
public class Project extends Properties {

    public static final SimpleDateFormat PROJECT_DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    public static final String CREATED = "created";
    private static final String ENV_HADOOP = "hadoop";
    private static final String ENV_EC2 = "ec2";
    private static final String FS_HDFS = "hdfs";
    private static final String FS_S3 = "s3";
    private static final String FS_LOCAL = "local";
    private static final String OUTPUT = "output";
    private static final String STAGING = "staging";
    private static final String INVENTORY = "inventory";
    private static final String RESULTS = "results";
    public static String ENV_LOCAL = "local";
    public static int DATA_SOURCE_EDISCOVERY = 0;
    public static int DATA_SOURCE_LOAD_FILE = 1;
    public static String PRODUCTION_FILE_NAME = "native";
    public static String METADATA_FILE_NAME = "metadata";
    private static Project currentProject = new Project();
    private String currentCustodian;
    private int mapItemStart = 1;
    private int mapItemEnd = 0;
    private int mapItemCurrent = 0;
    // this variable is for stopping local processing
    private boolean stopThePresses = false;

    public enum DATA {
        LOCAL, URI, PROBLEM
    }

    public static Project getCurrentProject() {
        return currentProject;
    }

    public static void setCurrentProject(Project aProject) {
        currentProject = aProject;
    }

    public static synchronized Project loadFromString(String str) {
        currentProject = new Project();
        if (str == null) {
            return currentProject;
        }
        try {
            currentProject.load(new StringReader(str.substring(0, str.length() - 1).replace(", ", "\n")));
            HashMap<String, String> map2 = new HashMap<>();
            for (java.util.Map.Entry<Object, Object> e : currentProject.entrySet()) {
                map2.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return currentProject;
    }

    public static synchronized Project loadFromFile(File file) {
        currentProject = new Project();
        try {
            currentProject.load(new FileReader(file));
            currentProject.setProjectFileName(file.getName());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return currentProject;
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

    /**
     * Remove all settings from project.
     *
     * @return Project
     */
    public static Project setEmptyProject() {
        currentProject = new Project();
        return currentProject;
    }

    /**
     * Return the true or false for a specific property. All true properties in
     * the Project setup are coded with either property-key=yes. Anything else,
     * such as key absent, value="no" or value = "false" results in false
     *
     * @param propertyKey the key we are checking
     * @return true if the property is present and its values is "true", and
     * false otherwise
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

    /**
     * @return the stopThePresses
     */
    synchronized public boolean isStopThePresses() {
        return stopThePresses;
    }

    /**
     * @param stopThePresses the stopThePresses to set
     */
    synchronized public void setStopThePresses(boolean stopThePresses) {
        this.stopThePresses = stopThePresses;
    }

    public String getProjectCode() {
        return getProperty(ParameterProcessing.PROJECT_CODE);
    }

    public Project setProjectCode(String projectCode) {
        setProperty(ParameterProcessing.PROJECT_CODE, projectCode);
        return this;
    }

    public String getLoadFileFormat() {
        return getProperty(ParameterProcessing.LOAD_FILE_FORMAT);
    }

    public Project setLoadFileFormat(String loadFileFormat) {
        setProperty(ParameterProcessing.LOAD_FILE_FORMAT, loadFileFormat);
        return this;
    }

    public String getProjectName() {
        return getProperty(ParameterProcessing.PROJECT_NAME);
    }

    public void setProjectName(String projectName) {
        setProperty(ParameterProcessing.PROJECT_NAME, projectName);
    }

    public String getNewProjectName() {
        return getProperty(ParameterProcessing.NEW_PROJECT_NAME);
    }

    public String getProjectFileName() {
        return getProperty(ParameterProcessing.PROJECT_FILE_NAME);
    }

    public void setProjectFileName(String fileName) {
        setProperty(ParameterProcessing.PROJECT_FILE_NAME, fileName);
    }

    public String getProjectFilePath() {
        return getProperty(ParameterProcessing.PROJECT_FILE_PATH);
    }

    public void setProjectFilePath(String filePath) {
        setProperty(ParameterProcessing.PROJECT_FILE_PATH, filePath);
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

    public String[] getDirsActive(String[] inputs) {
        String dirActive = getProperty(ParameterProcessing.DIR_ACTIVE);
        if (dirActive != null && !dirActive.trim().isEmpty()) {
            return dirActive.split(",");
        } else {
            String[] dirs = new String[inputs.length];
            for (int i = 0; i < dirs.length; ++i) {
                dirs[i] = "";
            }
            return dirs;
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

    public void setDirsActive(boolean[] dirsActive) {
        StringBuilder builder = new StringBuilder();
        for (boolean dir : dirsActive) {
            builder.append(dir ? "y" : "N").append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        setProperty(ParameterProcessing.DIR_ACTIVE, builder.toString());
    }

    public String getCullingAsTextBlock() {
        String culling = getProperty(ParameterProcessing.CULLING);
        if (culling == null) {
            return "";
        }
        String[] culls = culling.split(",");
        StringBuilder builder = new StringBuilder();
        for (String cull : culls) {
            builder.append(cull).append(ParameterProcessing.NL);
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
                + STAGING;
        return dir;
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
                return new File(getInventoryFileName()).exists();
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
                + OUTPUT;
        return dir;
    }

    public String getLoadFile() {
        return getResultsDir() + "/metadata." + getMetadataFileExt().toLowerCase();
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

    public String getCreated() {
        return getProperty(CREATED);
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

    public boolean needsOffice() {
        // TODO add all cases when Office services are needed
        return isCreatePDF();
    }

    public boolean isPreview() {
        return isPropertyTrue(ParameterProcessing.PREVIEW);
    }

    public void setPreview(boolean preview) {
        setProperty(ParameterProcessing.PREVIEW, Boolean.toString(preview));
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
     * @return One of the predefined separators, or the actual value string
     */
    public String getFieldSeparator() {
        return getProperty(ParameterProcessing.FIELD_SEPARATOR);
    }

    public void setFieldSeparator(String fieldSeparator) {
        setProperty(ParameterProcessing.FIELD_SEPARATOR, fieldSeparator);
    }

    public String getMetadataFileExt() {
        return getProperty(ParameterProcessing.METADATA_FILE_EXT);
    }

    public void setMetadataFileExt(String ext) {
        setProperty(ParameterProcessing.METADATA_FILE_EXT, ext);
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
        setProperty(ParameterProcessing.TEXT_IN_METADATA, Boolean.toString(b));
    }

    public String getCurrentCustodian() {
        return currentCustodian != null ? currentCustodian : "";
    }

    public Project setCurrentCustodian(String currentCustodian) {
        this.currentCustodian = currentCustodian.trim();
        return this;
    }

    public String getFormattedCustodian() {
        return currentCustodian.replaceAll(" ", "_");
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
        List<String> zipFiles = currentProject.getInventory();
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

    /**
     * Returns the value for the ocrEnabled parameter for this project.
     * <p>
     * Returns true only if the ocrEnabled is set to "true".
     * <p>
     * By default returns "false".
     *
     * @return
     */
    public boolean isOcrEnabled() {
        return isPropertyTrue(ParameterProcessing.OCR_ENABLED);
    }

    /**
     * Set the ocrEnabled parameter for this project. OCR processing is done
     * only if the ocrEnabled parameter is set to true.
     *
     * @param ocrEnabled
     */
    public void setOcrEnabled(boolean ocrEnabled) {
        setProperty(ParameterProcessing.OCR_ENABLED, Boolean.toString(ocrEnabled));
    }


    /**
     * Return true if the Send index to Solr is selected.
     *
     * @return
     */
    public boolean isSendIndexToSolrEnabled() {
        return isPropertyTrue(ParameterProcessing.SEND_INDEX_SOLR_ENABLED);
    }

    /**
     * Set the if the send to solr is enabled.
     *
     * @param enabled
     */
    public void setSendIndexToSolrEnabled(boolean enabled) {
        setProperty(ParameterProcessing.SEND_INDEX_SOLR_ENABLED, Boolean.toString(enabled));
    }

    public boolean isAddEmailAttachmentToPDF() {
        //not used at the moment
        //return isPropertyTrue(ParameterProcessing.ADD_EMAIL_ATTACHMENT_TO_PDF);
        return false;
    }

    /**
     * Set the if to add email attachments to generated PDFs
     *
     * @param enabled
     * @return
     */
    public Project setAddEmailAttachmentToPDF(boolean enabled) {
        setProperty(ParameterProcessing.ADD_EMAIL_ATTACHMENT_TO_PDF, Boolean.toString(enabled));
        return this;
    }

    public int getDataSource() {
        return Integer.parseInt(getProperty(ParameterProcessing.DATA_SOURCE));
    }

    public void setDataSource(int dataSource) {
        setProperty(ParameterProcessing.DATA_SOURCE, "" + dataSource);
    }

    public boolean isStageInPlace() {
        return isPropertyTrue(ParameterProcessing.STAGE_IN_PLACE);
    }

    public void setStageInPlace(boolean stageInPlace) {
        setProperty(ParameterProcessing.STAGE_IN_PLACE, Boolean.toString(stageInPlace));
    }

    public int getPiiLimit() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.PII_LIMIT));
        } catch (Exception e) {
            return 50;
        }
    }

    public int getSummarizeLimit() {
        try {
            return Integer.parseInt(getProperty(ParameterProcessing.SUMMARIZE_LIMIT));
        } catch (Exception e) {
            return 10;
        }
    }

    public void setPiiLimit(int piiLimit) {
        setProperty(ParameterProcessing.PII_LIMIT, "" + piiLimit);
    }

    public void setSummarizeLimit(int summarizeLimit) {
        setProperty(ParameterProcessing.SUMMARIZE_LIMIT, "" + summarizeLimit);
    }

    public String getPiiToken() {
        return getProperty(ParameterProcessing.PII_TOKEN);
    }

    public void setPiiToken(String piiToken) {
        setProperty(ParameterProcessing.PII_TOKEN, piiToken);
    }

    public String getPiiStatus() {
        String piiStatus = getProperty(ParameterProcessing.PII_STATUS);
        if (piiStatus == null || piiStatus.isEmpty()) {
            piiStatus = "Unknown";
        }
        return piiStatus;
    }

    public void setPiiStatus(String piiStatus) {
        setProperty(ParameterProcessing.PII_STATUS, piiStatus);
    }

    public boolean isPiiActive() {
        return isPropertyTrue(ParameterProcessing.PII_ACTIVE);
    }

    public void setPiiActive(boolean piiActive) {
        setProperty(ParameterProcessing.PII_ACTIVE, Boolean.toString(piiActive));
    }

    public boolean isSummarizeActive() {
        return isPropertyTrue(ParameterProcessing.SUMMARIZE_ACTIVE);
    }

    public void setSummarizeActive(boolean summarizeActive) {
        setProperty(ParameterProcessing.SUMMARIZE_ACTIVE, Boolean.toString(summarizeActive));
    }

    public boolean isPiiInabia() {
        return isPropertyTrue(ParameterProcessing.PII_INABIA);
    }

    public void setPiiInabia(boolean piiInabia) {
        setProperty(ParameterProcessing.PII_INABIA, Boolean.toString(piiInabia));
    }

    public String getSummarizeModel() {
        String model = getProperty(ParameterProcessing.SUMMARIZE_MODEL);
        if (model == null || model.isEmpty()) {
            return SummarizeText.models[0][0];
        } else {
            return model;
        }
    }

    public void setSummarizeMode(String summarizeModelCode) {
        setProperty(ParameterProcessing.SUMMARIZE_MODEL, summarizeModelCode);
    }

    public String getProcessingEngine() {
        String engine = getProperty(ParameterProcessing.PROCESSING_ENGINE);
        if (engine == null) {
            engine = "Standard";
        }
        return engine;
    }

    public void setProcessingEngine(String processingEngine) {
        setProperty(ParameterProcessing.PROCESSING_ENGINE, processingEngine);
    }
    public String getSparkMasterURL() {
        String sparkMasterUrl = getProperty(ParameterProcessing.SPARK_MASTER_URL);
        if (sparkMasterUrl == null) {
            sparkMasterUrl = "";
        }
        return sparkMasterUrl;
    }

    public void setSparkMasterUrl(String sparkMasterUrl) {
        setProperty(ParameterProcessing.SPARK_MASTER_URL, sparkMasterUrl);
    }

}
