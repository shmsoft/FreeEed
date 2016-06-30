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
import java.util.Iterator;
import org.freeeed.services.Mode;
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
            try (PreparedStatement stmt = conn.prepareStatement(
                    "update settings set field_value = ? where field_namew = ?")) {
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

    /**
     * Create settings table and fill it with initial values. If the table
     * exists, it won't be recreated
     *
     * @throws java.lang.Exception
     */
    static public void createSettingsTable() throws Exception {
        DbLocal dbLocal = DbLocal.getInstance();
        if (dbLocal.tableExists("settings")) {
            return;
        }
        try (Connection conn = dbLocal.createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("create table settings (field_name text, field_value text)");
            }
        }

        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement("insert into settings "
                    + "(field_name, field_value) values (?, ?)")) {
                String[][] initProperties = LocalSettings.getInitProperties();
                for (int i = 0; i < initProperties.length; ++i) {
                    pstmt.setString(1, initProperties[i][0]);
                    pstmt.setString(2, initProperties[i][1]);
                    pstmt.executeUpdate();
                }
            }
        }

    }
}
