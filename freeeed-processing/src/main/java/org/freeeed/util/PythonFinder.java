package org.freeeed.util;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.freeeed.services.Settings;

public class PythonFinder {
    private static final Logger LOGGER = Logger.getLogger(PythonFinder.class.getName());

    public static String findPython() {
        // 1. User-specified value from settings
        String configured = Settings.getSettings().getPythonExecutable();
        if (configured != null && !configured.isEmpty()) {
            File f = new File(configured);
            if (f.exists() && f.canExecute()) {
                return configured;
            }
            LOGGER.warning("Configured python executable not found: " + configured);
        }

        // 2. Linux + macOS automatic search
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

        // 3. Windows automatic search
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

        // Final fallback
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
