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
import java.sql.DriverManager;
import java.sql.SQLException;
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
    private static DbLocal INSTANCE;

    synchronized public static DbLocal getInstance() throws Exception {
        if (INSTANCE == null) {
            INSTANCE = new DbLocal();
        }
        return INSTANCE;
    }

    private DbLocal() throws Exception  {
        initDB();
    }

    /**
     * Find the mode from the local database. TODO find a more concise solution
     */
    public void loadMode() throws Exception {
        DbLocalUtils.createModeTable();
    }
    public Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
    }

    private void initDB() throws Exception {
        DbLocalUtils.createModeTable();
        DbLocalUtils.createSettingsTable();
    }

    public void loadSettings() throws Exception {
        DbLocalUtils.createSettingsTable();
    }
    public void saveSettings() {
        DbLocalUtils.saveSettings();
    }
    public void saveMode() {
        DbLocalUtils.saveMode();
    }
}
