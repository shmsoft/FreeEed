/*
 *
 * Copyright SHMsoft, Inc.
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
package org.freeeed.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Finds the review app's first-run administrator password so the desktop can
 * show it to the user.
 *
 * The review app no longer ships an admin/admin default (FreeEedUI #68): on
 * first start it generates a random password, keeps only the hash, and writes
 * the plaintext once to work/initial-admin-password.txt. That file lands in
 * Tomcat's working directory, which the user has no reason to know about -- so
 * without this, the security fix would simply lock people out of Review.
 *
 * The candidate list exists because the review app resolves that path relative
 * to whatever directory Tomcat was started from: start_dev_services.sh starts
 * it from freeeed-tomcat/bin, but a hand-started Tomcat may differ.
 */
public final class ReviewCredentials {
    private static final String PASSWORD_FILE = "work/initial-admin-password.txt";

    private static final String[] CANDIDATE_DIRS = {
        "freeeed-tomcat/bin",
        "freeeed-tomcat",
        ".",
    };

    private ReviewCredentials() {
    }

    /**
     * The first-run password file, or null when there isn't one -- which is the
     * normal case once the operator has changed the password and deleted it.
     */
    public static File findPasswordFile() {
        for (String dir : CANDIDATE_DIRS) {
            File f = new File(dir, PASSWORD_FILE);
            if (f.isFile() && f.canRead()) {
                return f;
            }
        }
        return null;
    }

    /** The generated password, or null if it can't be read. */
    public static String readPassword(File file) {
        return readField(file, "password:");
    }

    /** The account name the password belongs to; defaults to "admin". */
    public static String readUsername(File file) {
        String user = readField(file, "username:");
        return (user != null) ? user : "admin";
    }

    private static String readField(File file, String label) {
        if (file == null) {
            return null;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.toLowerCase().startsWith(label)) {
                    String value = trimmed.substring(label.length()).trim();
                    if (!value.isEmpty()) {
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            // Best-effort: the caller falls back to pointing at the file path.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ignore) {
                    // nothing useful to do
                }
            }
        }
        return null;
    }
}
