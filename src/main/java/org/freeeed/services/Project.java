/*    
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

import com.google.common.io.Files;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.freeeed.ec2.S3Agent;
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
            proj.load(new StringReader(str.substring(0, str.length() - 1).replace(", ", "\n")));
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
        try {
            skip = Integer.parseInt(getProperty(ParameterProcessing.SKIP));
        } catch (Exception e) {
            skip = 0;
        }
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
        setProperty(ParameterProcessing.SKIP, "" + skip);
    }

    public Project loadFromFile(File file) {
        Project proj = new Project();
        try {
            proj.load(new FileReader(file));
            proj.setProjectFileName(file.getName());
            proj.parseSkip();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return proj;
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
        String dir = getOutputDir() + File.separator + RESULTS;
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
        String createPdf = getProperty(ParameterProcessing.CREATE_PDF);
        if (createPdf != null) {
            return Boolean.valueOf(createPdf);
        }
        else {
            return false;
        }
    }

    public void setCreatePDF(boolean createPDF) {
        setProperty(ParameterProcessing.CREATE_PDF, Boolean.toString(createPDF));
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

    public boolean isRemoveSystemFiles() {
        String removeSystemFiles = getProperty(ParameterProcessing.REMOVE_SYSTEM_FILES);
        if (removeSystemFiles != null) {
            try {
                //backward compatibility
                if (removeSystemFiles.isEmpty()) {
                    return true;
                }
                
                return Boolean.parseBoolean(removeSystemFiles);
            } catch (Exception e) {
                return true;
            }
        }
        
        return true;
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
        return getProperty(ParameterProcessing.TEXT_IN_METADATA) != null;
    }

    public void setTextInMetadata(boolean b) {
        if (b) {
            setProperty(ParameterProcessing.TEXT_IN_METADATA, "");
        } else {
            remove(ParameterProcessing.TEXT_IN_METADATA);
        }
    }

    public void setupCurrentCustodianFromFilename(String fileName) {
        fileName = new File(fileName).getName();
        int underscore = fileName.indexOf("_");
        if (underscore >= 0 && underscore + 1 < fileName.length()) {
            currentCustodian = fileName.substring(underscore + 1, fileName.length() - 4);
        } else {
            currentCustodian = "";
        }
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
     * Set the ocrEnabled parameter for this project.
     * OCR processing is done only if the ocrEnabled parameter
     * is set to true.
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
    	String ocrEnabledStr = getProperty(ParameterProcessing.OCR_ENABLED);
    	if (ocrEnabledStr != null) {
            return Boolean.valueOf(ocrEnabledStr);
    	}
    	
    	return false;
    }

    /**
     * 
     * Set the value for Lucene FS index creation. If set to true,
     * Lucene FS index will be created during the document scan.
     * 
     * @param luceneFSIndexEnabled
     */
    public void setLuceneFSIndexEnabled(boolean luceneFSIndexEnabled) {
        setProperty(ParameterProcessing.LUCENE_FS_INDEX_ENABLED, Boolean.toString(luceneFSIndexEnabled));
    }
    
    /**
     * 
     * Return true if Lucene FS index creation is enabled.
     * 
     * @return
     */
    public boolean isLuceneFSIndexEnabled() {
        String luceneFSIndexEnabledStr = getProperty(ParameterProcessing.LUCENE_FS_INDEX_ENABLED);
        if (luceneFSIndexEnabledStr != null) {
            return Boolean.valueOf(luceneFSIndexEnabledStr);
        }
        
        return false;
    }
    
    /**
     * Set the if the send to solr is enabled.
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
        String sendIndexToSolrEnabledStr = getProperty(ParameterProcessing.SEND_INDEX_SOLR_ENABLED);
        if (sendIndexToSolrEnabledStr != null) {
            return Boolean.valueOf(sendIndexToSolrEnabledStr);
        }
        
        return true;
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
}
