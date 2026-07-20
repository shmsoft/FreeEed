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
package org.freeeed.main;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 *
 * @author mark
 */
public class Version {
    // Semantic version, shown in the About dialog. Bump this MANUALLY at
    // milestones, not per build: PATCH for a fix rollup, MINOR for a feature
    // (Viewer, Production), MAJOR for a breaking change. Drop -SNAPSHOT when
    // cutting an actual release. Daily builds keep this number; the git commit
    // SHA and build time below make each individual build uniquely traceable.
    private static final String V = "10.8.6-SNAPSHOT";

    // Written by git-commit-id-maven-plugin into the jar at build time.
    private static final Properties BUILD = loadBuildProperties();

    public static String getVersionAndBuild() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber() + getBuildSuffix();
    }

    public static String getVersion() {
        return ParameterProcessing.APP_NAME + " " + getVersionNumber();
    }

    public static String getVersionNumber() {
        return V;
    }

    /**
     * e.g. " (build 2026-06-21 14:03 UTC, g6d40400)" — empty when no build
     * info is bundled (running straight from source without the plugin).
     */
    private static String getBuildSuffix() {
        String sha = BUILD.getProperty("git.commit.id.abbrev", "");
        String time = BUILD.getProperty("git.build.time", "");
        boolean dirty = "true".equals(BUILD.getProperty("git.dirty", ""));
        if (sha.isEmpty() && time.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" (build");
        if (!time.isEmpty()) {
            sb.append(" ").append(time).append(" UTC");
        }
        if (!sha.isEmpty()) {
            sb.append(", g").append(sha);
            if (dirty) {
                sb.append("+"); // built from a working tree with uncommitted changes
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static Properties loadBuildProperties() {
        Properties props = new Properties();
        try (InputStream in = Version.class.getResourceAsStream("/git.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Build info is best-effort; fall back to the version number alone.
        }
        return props;
    }
}
