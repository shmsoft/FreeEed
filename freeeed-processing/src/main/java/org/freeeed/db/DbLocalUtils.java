/*
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.freeeed.services.Metadata;
import org.freeeed.services.Mode;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for local SQL db
 *
 * @author mark
 */
public class DbLocalUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbLocalUtils.class);

    /**
     * Create mode table and fill it with initial values. If the table exists,
     * it won't be recreated
     *
     * @throws java.lang.Exception
     */
    static public void createModeTable() throws Exception {
        DbLocal dbLocal = DbLocal.getInstance();
        if (dbLocal.tableExists("mode")) {
            return;
        }
        try (Connection conn = dbLocal.createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("create table mode (run_mode text)");
                stmt.execute("insert into mode (run_mode) values ('LOCAL')");
            }
        }
    }
    
    public static void createContentTypeMappingTable() throws Exception {
    	DbLocal dbLocal = DbLocal.getInstance();
        if (dbLocal.tableExists("content_type_mapping")) {
        	return;
        }
        try (Connection conn = dbLocal.createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("create table content_type_mapping (content_type text, file_type text)");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('message/rfc822', 'Email Message')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('text/html', 'HyperText Markup Language (HTML)')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/x-msdownload', 'Microsoft Application')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/msword', 'Microsoft Word')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'Microsoft Office - OOXML - Word Document')");
            
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/xhtml+xml', 'XHTML - The Extensible HyperText Markup Language')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/pdf', 'Adobe Portable Document Format')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('text/plain', 'Microsoft Office - OOXML - Word Document')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.ms-powerpoint', 'Microsoft PowerPoint')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.oasis.opendocument.presentation', 'OpenDocument Presentation')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'Microsoft Office - OOXML - Spreadsheet')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.ms-excel', 'Microsoft Excel')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/octet-stream', 'Binary Data')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/vnd.oasis.opendocument.text', 'OpenDocument Text')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('application/rtf', 'Rich Text Format')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('image/gif', 'Graphics Interchange Format')");
                stmt.execute("insert into content_type_mapping (content_type, file_type) values ('video/mpeg', 'MPEG Video')");
                
            }
        }
    }
    
    public static void createMetadataTable() throws Exception { 
    	DbLocal dbLocal = DbLocal.getInstance();
        if (dbLocal.tableExists("metadata")) {
        	return;
        }
        try (Connection conn = dbLocal.createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("create table metadata (key text, value text)");
                stmt.execute("insert into metadata (key, value) values ('01', 'UPI')");
                stmt.execute("insert into metadata (key, value) values ('02', 'File Name')");
                stmt.execute("insert into metadata (key, value) values ('03', 'Custodian')");
                stmt.execute("insert into metadata (key, value) values ('04', 'Source Device')");
                stmt.execute("insert into metadata (key, value) values ('05', 'Source Path, document_original_path')");
                stmt.execute("insert into metadata (key, value) values ('06', 'Production Path')");
                stmt.execute("insert into metadata (key, value) values ('07', 'Modified Date')");
                stmt.execute("insert into metadata (key, value) values ('08', 'Modified Time')");
                stmt.execute("insert into metadata (key, value) values ('09', 'Time Offset Value')");
                stmt.execute("insert into metadata (key, value) values ('10', 'processing_exception')");
                stmt.execute("insert into metadata (key, value) values ('11', 'master_duplicate')");
                stmt.execute("insert into metadata (key, value) values ('12', 'text')");
                stmt.execute("insert into metadata (key, value) values ('13', 'Creation Date, Creation-Date')");
                stmt.execute("insert into metadata (key, value) values ('21', 'To, Message-To')");
                stmt.execute("insert into metadata (key, value) values ('22', 'From, Author, Message-From')");
                stmt.execute("insert into metadata (key, value) values ('23', 'CC, Message-Cc')");
                stmt.execute("insert into metadata (key, value) values ('24', 'BCC, Message-Bcc')");
                stmt.execute("insert into metadata (key, value) values ('25', 'Date Sent')");
                stmt.execute("insert into metadata (key, value) values ('26', 'Time Sent')");
                stmt.execute("insert into metadata (key, value) values ('27', 'Subject, subject')");
                stmt.execute("insert into metadata (key, value) values ('28', 'Date Received, date')");
                stmt.execute("insert into metadata (key, value) values ('29', 'Time Received')");
                stmt.execute("insert into metadata (key, value) values ('31', 'native_link')");
                stmt.execute("insert into metadata (key, value) values ('32', 'text_link')");
                stmt.execute("insert into metadata (key, value) values ('33', 'exception_link')");
                stmt.execute("insert into metadata (key, value) values ('34', 'attachment_parent')");
                stmt.execute("insert into metadata (key, value) values ('35', 'message_id')");
                stmt.execute("insert into metadata (key, value) values ('36', 'references')");
                stmt.execute("insert into metadata (key, value) values ('37', 'File Type')");
                stmt.execute("insert into metadata (key, value) values ('38', 'Hash')");
            }
        }
    }

    /**
     * Load mode TODO - is there a more elegant way?
     *
     * @throws Exception
     */
    static public void loadMode() throws Exception {
        createModeTable();
        Mode mode = Mode.getInstance();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select * from mode")) {
                    if (resultSet.next()) {
                        String strMode = resultSet.getString("run_mode");
                        if (Mode.RUN_MODE.LOCAL.toString().equals(strMode)) {
                            mode.setRunMode(Mode.RUN_MODE.LOCAL);
                        } else if (Mode.RUN_MODE.AWS.toString().equals(strMode)) {
                            mode.setRunMode(Mode.RUN_MODE.AWS);
                        }
                    }
                }
            }
        }

    }

    static public void saveMode() {
        Settings setting = Settings.getSettings();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            String sql = "UPDATE mode SET run_mode = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, Mode.getInstance().getRunMode() + "");
                pstmt.execute();
            }
        } catch (Exception e) {
            LOGGER.error("DB problem", e);
        }
    }

    static public void saveSettings() throws Exception {
        Settings settings = Settings.getSettings();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("delete from settings");
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "insert into settings (field_name, field_value) values (?, ?)")) {
                Iterator iter = settings.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    String value = (String) settings.get(key);
                    stmt.setString(1, key);
                    stmt.setString(2, value);
                    stmt.execute();
                }
            }
        }
    }

    public static Metadata loadMetadata() throws Exception {
    	createMetadataTable();
    	Metadata metadata = new Metadata();
    	try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select * from metadata")) {
                    while (resultSet.next()) {
                        String key = resultSet.getString("key");
                        String value = resultSet.getString("value");
                        metadata.setProperty(key, value);
                    }
                }
            }
        }
    	return metadata;
    }
    
    public static Map<String, String> loadContentTypeMapping() throws Exception { 
    	createContentTypeMappingTable();
    	Map<String, String> ret = new HashMap<>();
    	try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select * from content_type_mapping")) {
                    while (resultSet.next()) {
                        String key = resultSet.getString("content_type");
                        String value = resultSet.getString("file_type");
                        ret.put(key, value);
                    }
                }
            }
        }
    	return ret;
    }
    
    static public void loadSettings() throws Exception {
        createSettingsTable();
        Settings settings = Settings.getSettings();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select * from settings")) {
                    while (resultSet.next()) {
                        String fieldName = resultSet.getString("field_name");
                        String fieldValue = resultSet.getString("field_value");
                        settings.setProperty(fieldName, fieldValue);
                    }
                }
            }
        }
    }

    static public Map<Integer, Project> getProjects() throws Exception {
        createProjectTable();

        Map<Integer, Project> projects = new HashMap<>();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "select * from project")) {
                    while (resultSet.next()) {
                        int projectId = resultSet.getInt("project_id");
                        Project project;
                        if (projects.containsKey(projectId)) {
                            project = projects.get(projectId);
                        } else {
                            project = new Project();
                            projects.put(projectId, project);
                        }
                        String fieldName = resultSet.getString("field_name");
                        project.put(fieldName, resultSet.getString("field_value"));
                        project.setProjectCode(Integer.toString(projectId));
                    }
                }
            }
        }
        return projects;
    }

    /**
     * Create settings table and fill it with initial values. If the table
     * exists, it won't be recreated
     *
     * @throws java.lang.Exception
     */
    static public void createSettingsTable() throws Exception {
        DbLocal dbLocal = DbLocal.getInstance();
        if (!dbLocal.tableExists("settings")) {
            try (Connection conn = dbLocal.createConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("create table settings (field_name text, field_value text)");
                }
            }
            try (Connection conn = DbLocal.getInstance().createConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement("insert into settings "
                        + "(field_name, field_value) values (?, ?)")) {
                    String[][] initProperties = LocalSettingsDefaults.getInitProperties();
                    for (int i = 0; i < initProperties.length; ++i) {
                        pstmt.setString(1, initProperties[i][0]);
                        pstmt.setString(2, initProperties[i][1]);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Create project table, put the first sample project there. If the table
     * exists, it won't be recreated.
     *
     * @throws java.lang.Exception
     */
    static public void createProjectTable() throws Exception {
        DbLocal dbLocal = DbLocal.getInstance();
        if (!dbLocal.tableExists("project")) {
            try (Connection conn = dbLocal.createConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("create table project (project_id int, field_name text, field_value text)");
                }
            }
            int projectId = 1;
            try (Connection conn = DbLocal.getInstance().createConnection()) {
                try (PreparedStatement pstmt = conn.prepareStatement("insert into project "
                        + "(project_id, field_name, field_value) values (?, ?, ?)")) {
                    String[][] initProperties = ProjectDefaults.getInitProperties();
                    for (int i = 0; i < initProperties.length; ++i) {
                        pstmt.setInt(1, projectId);
                        pstmt.setString(2, initProperties[i][0]);
                        pstmt.setString(3, initProperties[i][1]);
                        pstmt.executeUpdate();
                    }
                    pstmt.setInt(1, projectId);
                    pstmt.setString(2, Project.CREATED);
                    pstmt.setString(3, Project.projectDateFormat.format(new Date()));
                    pstmt.executeUpdate();
                }
            }
        }
    }

    static public void saveProject(Project project) throws Exception {
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("delete from project where project_id = " + project.getProjectCode());
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "insert into project (project_id, field_name, field_value) values (?, ?, ?)")) {
                Iterator iter = project.keySet().iterator();
                while (iter.hasNext()) {
                    int projectId = Integer.parseInt(project.getProjectCode());
                    String key = (String) iter.next();
                    String value = (String) project.get(key);
                    stmt.setInt(1, projectId);
                    stmt.setString(2, key);
                    stmt.setString(3, value);
                    stmt.execute();
                }
            }
        }
    }

    static public void deleteProject(int projectId) throws Exception {
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("delete from project where project_id = " + projectId);
            }
        }
    }

    static public Project createNewProject() throws Exception {
        int projectId = 0;
        try (Connection conn = DbLocal.getInstance().createConnection()) {            
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery("select max(project_id) from project")) {
                    if (resultSet.next()) {
                        projectId = resultSet.getInt(1) + 1;
                    }
                }
            }
            try (PreparedStatement pstmt = conn.prepareStatement("insert into project "
                    + "(project_id, field_name, field_value) values (?, ?, ?)")) {
                String[][] initProperties = ProjectDefaults.getInitProperties();
                for (int i = 0; i < initProperties.length; ++i) {
                    // for projects other than the first sample one, remove "input"
                    if (projectId > 1) {
                        if ("input".equals(initProperties[i][0])) {
                            continue;
                        }
                    }
                    pstmt.setInt(1, projectId);
                    pstmt.setString(2, initProperties[i][0]);
                    pstmt.setString(3, initProperties[i][1]);
                    pstmt.executeUpdate();
                }
                pstmt.setInt(1, projectId);
                pstmt.setString(2, Project.CREATED);
                pstmt.setString(3, Project.projectDateFormat.format(new Date()));
                pstmt.executeUpdate();
            }
        }
        return getProject(projectId);
    }
    
    static public Project getProject(int projectId) throws Exception {        
        Project project = new Project();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "select * from project where project_id = " + projectId)) {
                    while (resultSet.next()) {                                                
                        String fieldName = resultSet.getString("field_name");
                        project.put(fieldName, resultSet.getString("field_value"));
                        project.setProjectCode(Integer.toString(projectId));
                    }
                }
            }
        }
        return project;
    }    
}
