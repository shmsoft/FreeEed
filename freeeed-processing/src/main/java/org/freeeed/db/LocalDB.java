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

/**
 * Connector to local db. As a design pattern, this could be just a facade. But right now, this db
 * is SQLite anyway, so if this changes, we'll make it a facade; for now, it is direct.
 *
 * @author mark
 */
public class LocalDB {

    /**
     * Singleton
     */
    private static final LocalDB instance = new LocalDB();

    public static LocalDB getInstance() {
        return instance;
    }

    private LocalDB() {
    }

    public boolean isLocalMode() {
        try (Connection conn = createConnection()) {
            // use connection 
        } catch (Exception e) {
            // handle exception
        }
        return true;
    }

    public void setLocalModel(boolean b) {

    }

    private void createSettingsTable() {
        
    }
    private Connection createConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection("jdbc:sqlite:test.db");
    }
}
