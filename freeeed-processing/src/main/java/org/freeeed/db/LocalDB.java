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
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector to local db. As a design pattern, this could be just a facade. But
 * right now, this db is SQLite anyway, so if this changes, we'll make it a
 * facade; for now, it is direct.
 *
 * @author mark
 */
public class LocalDB {
private static final Logger logger = LoggerFactory.getLogger(LocalDB.class);
    /**
     * Singleton
     */
    private static final LocalDB instance = new LocalDB();

    public static LocalDB getInstance() {
        return instance;
    }

    private LocalDB() {
        initDB();
    }

    public void loadSettings(Settings settings) {
        try (Connection conn = createConnection()) {
            String sql = "SELECT * FROM SETTINGS";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String mode = rs.getString("mode");
                if (Settings.MODE.LOCAL.toString().equalsIgnoreCase(mode)) {
                    settings.setMode(Settings.MODE.LOCAL);
                } else if (Settings.MODE.AWS.toString().equalsIgnoreCase(mode)) {
                    settings.setMode(Settings.MODE.AWS);
                } else {
                    throw new Exception("Mode local/aws could not be determined");
                }
            }
        } catch (Exception e) {
            logger.error("DB problem", e);
        }
    }

    private void createSettingsTable() {
        try (Connection conn = createConnection()) {
            String sql = "create table if not exists settings (mode text)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.execute();
            
        } catch (Exception e) {
            logger.error("DB problem", e);
        }

    }

    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }

    private void initDB() {
        createSettingsTable();
    }
}
