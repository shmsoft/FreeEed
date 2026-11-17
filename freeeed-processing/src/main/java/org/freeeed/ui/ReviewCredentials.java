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

    /** Sibling of the password file; rewritten on every user-store change. */
    private static final String USER_STORE_FILE = "u.dat";

    private static final String[] CANDIDATE_DIRS = {
        "freeeed-tomcat/bin",
        "freeeed-tomcat",
        ".",
    };

    /**
     * Bases each candidate is tried against.
     *
     * The Player does NOT run from the pack root: open_player.sh cds into
     * FreeEed/ first, so every relative path inside the Player is one level
     * below the directory that holds freeeed-tomcat. Without "..", the lookup
     * missed on every installed build and the first-sign-in dialog silently
     * never appeared -- the file was there, correct and mode 600, and nothing
     * went looking in the right place. The Control Panel runs from the root and
     * so always worked, which is why this survived review.
     *
     * Same bug bit org.freeeed.main.Version reading freeeedui.properties. If a
     * third relative lookup shows up in the Player, resolve the pack root once
     * and share it rather than adding a fourth copy of this list.
     */
    private static final String[] BASE_DIRS = {".", ".."};

    private ReviewCredentials() {
    }

    /**
     * The first-run password file, or null when there isn't one -- which is the
     * normal case once the operator has changed the password and deleted it.
     */
    public static File findPasswordFile() {
        for (String base : BASE_DIRS) {
            for (String dir : CANDIDATE_DIRS) {
                File f = new File(new File(base, dir), PASSWORD_FILE);
                if (f.isFile() && f.canRead()) {
                    // Canonical, because the dialog prints this path for the
                    // user to go and delete. A literal ".." in it invites them
                    // to delete the wrong thing.
                    try {
                        return f.getCanonicalFile();
                    } catch (java.io.IOException e) {
                        return f;
                    }
                }
            }
        }
        return null;
    }

    /**
     * True when the user store has been written since this file was, i.e. the
     * generated password can no longer be relied on.
     *
     * The review app writes the password file once, when it creates the admin
     * account, and (before FreeEedUI#95) never again -- so changing the password
     * leaves the file showing a credential the login form rejects. The desktop
     * must not present that as "the password generated for this installation".
     *
     * work/u.dat is rewritten on every change to the user store, so a user store
     * newer than the password file means the file may be out of date. On a fresh
     * install the order is the other way round -- createAdminUser() stores the
     * users first and publishes the password second -- so a genuine first run is
     * never mistaken for a stale one.
     *
     * This errs towards saying nothing. An unrelated edit (adding a reviewer)
     * also touches the user store and will suppress the dialog even though the
     * generated password still works -- but anyone who has added a user has
     * already signed in, and withholding a password is recoverable where
     * asserting a wrong one is not.
     */
    public static boolean isSuperseded(File passwordFile) {
        if (passwordFile == null) {
            return false;
        }

        File userStore = new File(passwordFile.getParentFile(), USER_STORE_FILE);
        if (!userStore.isFile()) {
            return false;
        }

        long storeModified = userStore.lastModified();
        long fileModified = passwordFile.lastModified();

        // 0 means "unknown" from File.lastModified(); do not guess from it.
        if (storeModified == 0L || fileModified == 0L) {
            return false;
        }

        return storeModified > fileModified;
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
