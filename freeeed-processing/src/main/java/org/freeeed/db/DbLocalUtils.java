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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.freeeed.services.Mode;
import org.freeeed.services.Project;
import org.freeeed.services.ProjectInfo;
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

    /**
     * Load mode TODO - is there a more elegant way?
     *
     * @throws Exception
     */
    static public void loadMode() throws Exception {
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
                // TODO is there a type safe way to do this?
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

    static public void loadSettings() throws Exception {
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

    static public void loadProject(int projectId) throws Exception {
        Project project = Project.getProject();
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "select * from project where project_id = " + projectId)) {
                    while (resultSet.next()) {
                        String fieldName = resultSet.getString("field_name");
                        String fieldValue = resultSet.getString("field_value");
                        project.setProperty(fieldName, fieldValue);
                    }
                }
            }
        }
    }

    static public List<ProjectInfo> getProjects() throws Exception {
        List<ProjectInfo> projects = new ArrayList<>();        
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet resultSet = stmt.executeQuery(
                        "select * from project")) {
                    while (resultSet.next()) {
                        ProjectInfo info = new ProjectInfo();
                        info.setId(resultSet.getInt("id"));
                        info.setName(resultSet.getString("name"));
                        info.setDescription(resultSet.getString("description"));
                        info.setCreated(resultSet.getDate("create"));
                        projects.add(info);
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
                }
            }
        }
    }

    static public void getProject(int projectId) {
        Project project = Project.getProject();

    }
}
