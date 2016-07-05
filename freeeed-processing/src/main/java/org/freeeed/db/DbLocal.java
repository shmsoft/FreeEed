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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector to local db. It stores the bootstrap mode and the projects if this
 * mode is local.
 *
 * @author mark
 */
public class DbLocal {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbLocal.class);
    private static final String DB_NAME = "freeeed.db";
    /**
     * Singleton
     */
    private static final DbLocal INSTANCE = new DbLocal();

    synchronized public static DbLocal getInstance() {
        return INSTANCE;
    }

    private DbLocal() {
    }

    /**
     * Find the mode from the local database. TODO find a more concise solution
     *
     * @throws java.lang.Exception
     */
    public void loadMode() throws Exception {
        DbLocalUtils.createModeTable();
        DbLocalUtils.loadMode();
    }

    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
    }

    public void loadProject(int projectId) throws Exception {
        DbLocalUtils.createProjectTable();
        DbLocalUtils.loadProject(projectId);
    }

    public Map<Integer, Project> getProjects() throws Exception {
        DbLocalUtils.createProjectTable();
        return DbLocalUtils.getProjects();
    }

    public void loadSettings() throws Exception {
        DbLocalUtils.createSettingsTable();
        DbLocalUtils.loadSettings();
    }

    public void saveSettings() throws Exception {
        DbLocalUtils.saveSettings();
    }

    public void saveMode() {
        DbLocalUtils.saveMode();
    }

    public boolean tableExists(String tableName) throws Exception {
        boolean answer = false;
        try (Connection conn = createConnection()) {
            DatabaseMetaData metadata = conn.getMetaData();
            try (ResultSet resultSet = metadata.getTables(null, null, tableName, null)) {
                if (resultSet.next()) {
                    answer = true;
                }
            }
        }
        LOGGER.debug("Table {} exists? - {}", tableName, answer);
        return answer;
    }
}
