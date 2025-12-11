package org.freeeed.util;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PythonFinder {
    private static final Logger LOGGER = Logger.getLogger(PythonFinder.class.getName());

    /**
     * Find python using environment/system overrides or automatic detection.
     * This no-arg overload will check env/system properties and fall back to automatic search.
     */
    public static String findPython() {
        return findPython(null);
    }

    /**
     * Resolve a python executable. Priority:
     * 1) env FREEEED_PYTHON
     * 2) system property freeeed.python
     * 3) caller-supplied configured path
     * 4) PATH and common locations
     *
     * @param configured caller-supplied configured path (may be null/empty)
     * @return resolved python executable path or "python" fallback
     */
    public static String findPython(String configured) {
        // 1) Environment variable override
        String env = System.getenv("FREEEED_PYTHON");
        if (env != null && !env.isEmpty()) {
            File f = new File(env);
            if (f.exists() && f.canExecute()) return env;
            LOGGER.warning("FREEEED_PYTHON env set but not executable: " + env);
        }

        // 2) System property override
        String prop = System.getProperty("freeeed.python");
        if (prop != null && !prop.isEmpty()) {
            File f = new File(prop);
            if (f.exists() && f.canExecute()) return prop;
            LOGGER.warning("System property freeeed.python set but not executable: " + prop);
        }

        // 3) Caller-supplied configured value
        if (configured != null && !configured.isEmpty()) {
            File f = new File(configured);
            if (f.exists() && f.canExecute()) return configured;
            LOGGER.warning("Configured python executable not found: " + configured);
        }

        // 4) Platform automatic search
        if (isNix()) {

            // First try PATH
            if (commandExists("python3")) return resolveCommand("python3");
            if (commandExists("python")) return resolveCommand("python");

            // Common Unix paths
            String home = System.getProperty("user.home");
            String[] unixPaths = {
                    home + "/.pyenv/shims/python",
                    home + "/.pyenv/shims/python3",
                    home + "/miniconda3/bin/python",
                    home + "/anaconda3/bin/python",
                    "/opt/homebrew/bin/python3",      // macOS ARM
                    "/usr/local/bin/python3",
                    "/usr/bin/python3"
            };

            for (String p : unixPaths) {
                if (new File(p).exists()) return p;
            }
        }

        if (isWindows()) {

            // PATH
            if (commandExists("python.exe")) return resolveCommand("python.exe");
            if (commandExists("py.exe")) return resolveCommand("py.exe");

            String home = System.getProperty("user.home");

            String[] winPaths = {
                    "C:\\Python313\\python.exe",
                    "C:\\Python312\\python.exe",
                    "C:\\Python311\\python.exe",
                    "C:\\Python310\\python.exe",

                    home + "\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                    home + "\\AppData\\Local\\Programs\\Python\\Python312\\python.exe",
                    home + "\\AppData\\Local\\Programs\\Python\\Python311\\python.exe",
                    home + "\\AppData\\Local\\Programs\\Python\\Python310\\python.exe"
            };

            for (String p : winPaths) {
                if (new File(p).exists()) return p;
            }
        }

        LOGGER.warning("No Python interpreter found. Falling back to 'python'.");
        return "python";
    }

    // ----------------------------
    // Helper methods
    // ----------------------------

    private static boolean commandExists(String cmd) {
        try {
            ProcessBuilder pb;

            if (isWindows()) {
                pb = new ProcessBuilder("where", cmd);
            } else {
                pb = new ProcessBuilder("which", cmd);
            }

            Process p = pb.start();
            // Wait for process to finish within timeout and ensure exit code is 0
            boolean finished = p.waitFor(2, TimeUnit.SECONDS);
            return finished && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static String resolveCommand(String cmd) {
        try {
            ProcessBuilder pb;

            if (isWindows()) {
                pb = new ProcessBuilder("where", cmd);
            } else {
                pb = new ProcessBuilder("which", cmd);
            }

            Process p = pb.start();
            p.waitFor(2, TimeUnit.SECONDS);

            String output = new String(p.getInputStream().readAllBytes()).trim();
            if (!output.isEmpty()) {
                // "where" can return multiple lines – use the first
                return output.split("\\R")[0];
            }
        } catch (Exception ignored) {}

        return cmd; // Fallback
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private static boolean isNix() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("mac");
    }
}
