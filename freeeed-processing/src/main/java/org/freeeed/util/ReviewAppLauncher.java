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
package org.freeeed.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Makes sure the FreeEed review web app (Tomcat, :8090) is running before
 * processing tries to register a case with it.
 *
 * <p>Background: case creation POSTs to the review app. If review was down that
 * POST silently failed and processing still reported "Case created" - so the
 * user ended up with data in Solr but no case in review, and no idea why. Most
 * users don't know how to start the review app, so instead of just warning we
 * start the bundled Tomcat for them.
 *
 * <p>We also run the bundled {@code shutdown} script first: a leftover/hung
 * Tomcat can keep the shutdown port and strand :8090 (seen in the field), and a
 * graceful shutdown clears it before we start fresh. Tomcat is pointed at the
 * app's own bundled JRE ({@code java.home}) so it can't fail for a missing
 * JAVA_HOME.
 */
public final class ReviewAppLauncher {

    private static final java.util.logging.Logger LOGGER =
            LogFactory.getLogger(ReviewAppLauncher.class.getName());

    private static final int START_WAIT_SECONDS = 40;

    private ReviewAppLauncher() {
    }

    /**
     * Ensure the review app is reachable, starting it if needed.
     *
     * @param reviewEndpoint e.g. http://localhost:8090/freeeedui
     * @return true if review is up (already, or after we started it); false if
     *         it was down and could not be started.
     */
    public static boolean ensureReviewUp(String reviewEndpoint) {
        if (isReviewUp(reviewEndpoint)) {
            return true;
        }

        LOGGER.info("Review app not reachable at " + reviewEndpoint + " - attempting to start it...");
        if (!startReviewApp()) {
            LOGGER.severe("Could not locate or launch the bundled review app (Tomcat).");
            return false;
        }

        for (int i = 0; i < START_WAIT_SECONDS; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
            if (isReviewUp(reviewEndpoint)) {
                LOGGER.info("Review app is up.");
                return true;
            }
        }

        LOGGER.severe("Review app did not come up within " + START_WAIT_SECONDS + "s.");
        return false;
    }

    /**
     * True if Tomcat answers at all - any HTTP status (200/302/401/403...) means
     * it is serving; a connection refused / timeout means it is down.
     */
    public static boolean isReviewUp(String reviewEndpoint) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(reviewEndpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            return conn.getResponseCode() > 0;
        } catch (IOException e) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static boolean startReviewApp() {
        File tomcatBin = findTomcatBin();
        if (tomcatBin == null) {
            return false;
        }
        File tomcatHome = tomcatBin.getParentFile();
        boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");

        File startup = new File(tomcatBin, windows ? "startup.bat" : "startup.sh");
        File shutdown = new File(tomcatBin, windows ? "shutdown.bat" : "shutdown.sh");

        if (!startup.exists()) {
            LOGGER.severe("Tomcat start script not found: " + startup.getAbsolutePath());
            return false;
        }

        // Best-effort: clear any existing/hung instance (may hold the shutdown
        // port and strand :8090). Ignore the result.
        runTomcatScript(shutdown, tomcatHome, windows);
        // Start it.
        return runTomcatScript(startup, tomcatHome, windows);
    }

    private static boolean runTomcatScript(File script, File tomcatHome, boolean windows) {
        if (!script.exists()) {
            return false;
        }
        try {
            ProcessBuilder pb = windows
                    ? new ProcessBuilder("cmd.exe", "/c", script.getAbsolutePath())
                    : new ProcessBuilder("sh", script.getAbsolutePath());
            pb.directory(tomcatHome);

            // Point Tomcat at the app's own bundled JRE so it can't fail for a
            // missing JAVA_HOME (a real field failure on machines with only a
            // stray JDK on PATH).
            String javaHome = System.getProperty("java.home");
            if (javaHome != null && !javaHome.isEmpty()) {
                pb.environment().put("JAVA_HOME", javaHome);
            }
            pb.redirectErrorStream(true);
            pb.start();
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to run " + script.getName() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Locate {@code freeeed-tomcat/bin} across the likely install layouts
     * (mirrors how ControlPanelUI resolves the start scripts: user.dir, then one
     * or two levels up).
     */
    private static File findTomcatBin() {
        String userDir = System.getProperty("user.dir", ".");
        File[] candidates = new File[] {
            new File(userDir, "freeeed-tomcat/bin"),
            new File(userDir, "../freeeed-tomcat/bin"),
            new File(userDir, "../../freeeed-tomcat/bin")
        };
        for (File c : candidates) {
            if (new File(c, "startup.sh").exists() || new File(c, "startup.bat").exists()) {
                return c;
            }
        }
        LOGGER.severe("Could not find freeeed-tomcat/bin under user.dir=" + userDir);
        return null;
    }
}
