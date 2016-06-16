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
import java.sql.Statement;
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
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("create table mode (run_mode text)");
                stmt.execute("insert into mode (run_mode) values ('LOCAL')");
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

    static public void saveSettings() {

    }

    /**
     * Create settings table and fill it with initial values. If the table
     * exists, it won't be recreated
     *
     * @throws java.lang.Exception
     */
    static public void createSettingsTable() throws Exception {
        boolean tableCreated;
        try (Connection conn = DbLocal.getInstance().createConnection()) {
            try (Statement stmt = conn.createStatement()) {
                tableCreated = stmt.execute("create table settings (field_name text, field_value text)");
            }
        }
        if (tableCreated) {
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
}
