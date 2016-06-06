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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.freeeed.services.Mode;
import org.freeeed.services.Settings;
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

    public static DbLocal getInstance() {
        return INSTANCE;
    }

    private DbLocal() {
        initDB();
    }

    /**
     * Find the mode from the local database. TODO find a more concise solution
     */
    public void loadMode() {
        Mode mode = Mode.getInstance();
        try (Connection conn = createConnection()) {
            String sql = "SELECT * FROM mode";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String runMode = rs.getString("run_mode");
                if (Mode.RUN_MODE.LOCAL.toString().equalsIgnoreCase(runMode)) {
                    mode.setRunMode(Mode.RUN_MODE.LOCAL);
                } else if (Mode.RUN_MODE.AWS.toString().equalsIgnoreCase(runMode)) {
                    mode.setRunMode(Mode.RUN_MODE.AWS);
                } else {
                    throw new Exception("Run mode could not be determined");
                }
            }
        } catch (Exception e) {
            LOGGER.error("DB problem", e);
        }
    }

    private void createSettingsTable() {
        try (Connection conn = createConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("create table settings (run_mode text)");
            stmt.execute("insert into settings (run_mode) values ('LOCAL')");
        } catch (SQLException e) {
            LOGGER.debug("Table settings found");
        } catch (Exception e) {
            LOGGER.error("Local DB problem", e);
        }

    }

    private void createModeTable() {
        try (Connection conn = createConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute("create table mode (run_mode text)");
            stmt.execute("insert into mode (run_mode) values ('LOCAL')");
        } catch (SQLException e) {
            LOGGER.debug("Table 'mode' already present, no need to recreate it");
        } catch (Exception e) {
            LOGGER.error("Local DB problem", e);
        }

    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:" + DB_NAME);
    }

    private void initDB() {
        createModeTable();
    }

    public void saveSettings(Settings settings) {
        Settings setting = Settings.getSettings();
        try (Connection conn = createConnection()) {
//            String sql = "update settings set run_mode = ?";
//            PreparedStatement pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, settings.getMode() + "");
//            pstmt.execute();
        } catch (Exception e) {
            LOGGER.error("DB problem", e);
        }
    }

    public void saveMode() {
        Settings setting = Settings.getSettings();
        try (Connection conn = createConnection()) {
            String sql = "UPDATE mode SET run_mode = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, Mode.getInstance().getRunMode() + "");
            pstmt.execute();
        } catch (Exception e) {
            LOGGER.error("DB problem", e);
        }
    }
}
