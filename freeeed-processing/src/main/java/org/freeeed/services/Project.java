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
import org.freeeed.main.ParameterProcessing;
import org.freeeed.util.Util;

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

    private static Project currentProject = new Project();
    public static final SimpleDateFormat PROJECT_DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH:mm");
    private static final String ENV_HADOOP = "hadoop";
    public static String ENV_LOCAL = "local";
    private static final String ENV_EC2 = "ec2";
    private static final String FS_HDFS = "hdfs";
    private static final String FS_S3 = "s3";
    private static final String FS_LOCAL = "local";
    private static final String OUTPUT = "output";
    private static final String STAGING = "staging";
    private static final String INVENTORY = "inventory";
    private static final String RESULTS = "results";
    public static final String CREATED = "created";
    public static final String DELETED = "deleted";
    public static int DATA_SOURCE_EDISCOVERY = 0;
    public static int DATA_SOURCE_LOAD_FILE = 1;
    public static int DATA_SOURCE_BLOCKCHAIN = 2;
    public static int DATA_SOURCE_QB = 3;
    public static String PRODUCTION_FILE_NAME = "native";
    public static String METADATA_FILE_NAME = "metadata";

    private String currentCustodian;
    private int mapItemStart = 1;
    private int mapItemEnd = 0;
    private int mapItemCurrent = 0;
    // this variable is for stopping local processing
    private boolean stopThePresses = false;

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

    public enum DATA {

        LOCAL, URI, PROBLEM
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
        return getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT + File.separator
                + STAGING;
    }

    public String getInventoryFileName() {
        return getStagingDir() + File.separator + INVENTORY;
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
            } else {
                // TODO check for valid URI
                locationType = DATA.URI;
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
        return getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT;
    }

    public String getLoadFile() {
        return getResultsDir() + "/metadata" + ParameterProcessing.METADATA_FILE_EXT;
    }

    public String getResultsDir() {
        return getOutputDir() + File.separator + RESULTS;
    }

    public String getRunsDir() {
        return getOut() + File.separator
                + getProjectCode() + File.separator
                + OUTPUT;
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

    public boolean isDeleted() {
        return "yes".equalsIgnoreCase(getProperty(DELETED));
    }

    public Project setDeleted(boolean b) {
        setProperty(Project.DELETED, "" + (b ? "yes" : "no"));
        return this;
    }

    public double getGigsPerArchive() {
        try {
            return Double.parseDouble(getProperty(ParameterProcessing.GIGS_PER_ZIP_STAGING));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public void setGigsPerArchive(double d) {
        setProperty(ParameterProcessing.GIGS_PER_ZIP_STAGING, Double.toString(d));
    }

    public double getSamplePercent() {
        try {
            return Double.parseDouble(getProperty(ParameterProcessing.SAMPLE_PERCENT));
        } catch (Exception e) {
            return 0;
        }
    }

    public void setSamplePercent(double d) {
        setProperty(ParameterProcessing.SAMPLE_PERCENT, Double.toString(d));
    }

    public boolean isCreatePDF() {
        return isPropertyTrue(ParameterProcessing.CREATE_PDF);
    }

    public boolean needsOffice() {
        // TODO add all cases when Office services are needed
        return isCreatePDF();
    }

    public void setCreatePDF(boolean createPDF) {
        setProperty(ParameterProcessing.CREATE_PDF, Boolean.toString(createPDF));
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

    public void setupCurrentCustodianFromFilename(String fileName) {
        currentCustodian = "";
        int lastUnderscore = fileName.lastIndexOf("_");
        if (lastUnderscore > 0) {
            currentCustodian = fileName.substring(lastUnderscore + 1);
        }
        String extension = Util.getExtension(currentCustodian);
        if (extension != null && extension.length() > 0) {
            currentCustodian = currentCustodian.substring(0,
                    currentCustodian.length() - 1 - extension.length());
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
     * Returns the value for the ocrEnabled parameter for this project.
     * <p>
     * Returns true only if the ocrEnabled is set to "true".
     * <p>
     * By default returns "false".
     *
     * @return
     */
    public boolean isOcrEnabled() {
        String property = getProperty(ParameterProcessing.OCR_ENABLED);
        return property == null || Boolean.valueOf(property);
    }

    /**
     * Set the value for Lucene FS index creation. If set to true, Lucene FS
     * index will be created during the document scan.
     *
     * @param luceneIndexEnabled
     */
    public void setLuceneIndexEnabled(boolean luceneIndexEnabled) {
        setProperty(ParameterProcessing.LUCENE_FS_INDEX_ENABLED, Boolean.toString(luceneIndexEnabled));
    }

    /**
     * Return true if Lucene index creation is enabled.
     *
     * @return
     */
    public boolean isLuceneIndexEnabled() {
        return isPropertyTrue(ParameterProcessing.LUCENE_FS_INDEX_ENABLED);
    }

    /**
     * Set the if the send to elastic search is enabled.
     *
     * @param enabled
     */
    public void setSendIndexToESEnabled(boolean enabled) {
        setProperty(ParameterProcessing.SEND_INDEX_ES_ENABLED, Boolean.toString(enabled));
    }

    /**
     * Return true if the Send index to Elastic Search is selected.
     *
     * @return
     */
    public boolean isSendIndexToESEnabled() {
        return isPropertyTrue(ParameterProcessing.SEND_INDEX_ES_ENABLED);
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

    public boolean isAddEmailAttachmentToPDF() {
        //not used at the moment
        //return isPropertyTrue(ParameterProcessing.ADD_EMAIL_ATTACHMENT_TO_PDF);
        return false;
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

    public int getDataSource() {
        return Integer.parseInt(getProperty(ParameterProcessing.DATA_SOURCE));
    }

    public void setDataSource(int dataSource) {
        setProperty(ParameterProcessing.DATA_SOURCE, "" + dataSource);
    }

    public void setBlockFrom(int from) {
        setProperty(ParameterProcessing.FROM_BLOCK, "" + from);
    }

    public void setBlockTo(int to) {
        setProperty(ParameterProcessing.TO_BLOCK, "" + to);
    }

    public int getBlockFrom() {
        String property = getProperty(ParameterProcessing.FROM_BLOCK);
        return property == null ? 1 : Integer.parseInt(property);
    }

    public int getBlockTo() {
        String property = getProperty(ParameterProcessing.TO_BLOCK);
        return property == null ? 10 : Integer.parseInt(property);
    }


    public void setStageInPlace(boolean stageInPlace) {
        setProperty(ParameterProcessing.STAGE_IN_PLACE, Boolean.toString(stageInPlace));
    }

    public boolean isStageInPlace() {
        return isPropertyTrue(ParameterProcessing.STAGE_IN_PLACE);
    }

}
