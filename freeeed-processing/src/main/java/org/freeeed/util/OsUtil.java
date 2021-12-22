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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import org.freeeed.services.Settings;

public class OsUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(OsUtil.class);
    private List<String> buffer = new ArrayList<>();
    // cached results of system check
    private static boolean hasReadpst;
    private static boolean hasWkhtmltopdf;
    private static boolean hasSOffice;
    private static String readPstExecutableLocation;
    private static String sofficeExecutableLocation;
    private static String wkhtmltopdfExecutableLocation;

    private static final String READPST_VERSION = "ReadPST / LibPST v0.6.66";

    // For debugging Windows code flow
    private static boolean debuggingWindows = false;

    /**
     * @return the readpst
     */
    public static boolean hasReadpst() {
        return hasReadpst;
    }

    /**
     * @return whether the platform has wkhtmltopdf installed
     */
    public static boolean hasWkhtmltopdf() {
        return hasWkhtmltopdf;
    }

    public static boolean hasSOffice() {
        return hasSOffice;
    }

    public static enum OS {
        LINUX, WINDOWS, MACOSX, UNKNOWN
    };

    /**
     * Determine the underlying OS.
     *
     * @return OS on which we are running
     */
    static public OS getOs() {
        if (debuggingWindows) return OS.WINDOWS;
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.startsWith("windows")) {
            return OS.WINDOWS;
        } else if (platform.startsWith("linux")) {
            return OS.LINUX;
        } else if (platform.startsWith("mac os x")) {
            return OS.MACOSX;
        } else {
            return OS.UNKNOWN;
        }
    }

    /**
     * Determine if we are running on Unix (Linux or Mac OS).
     *
     * @return true if running on *nix, false if not.
     */
    public static boolean isNix() {
        OS os = getOs();
        return (os == OS.LINUX || os == OS.MACOSX);
    }

    /**
     * Determine if we are running on Linux.
     *
     * @return true if running on Linix, false if not.
     */
    public static boolean isLinux() {
        return (getOs() == OS.LINUX);
    }

    /**
     * Determine if we are running on Mac OS.
     *
     * @return true if running on Mac, false if not.
     */
    public static boolean isMac() {
        return (getOs() == OS.MACOSX);
    }

    /**
     * Determine if we are running on Windows.
     *
     * @return true if running on Windows, false if not.
     */
    public static boolean isWindows() {
        OS os = getOs();
        return (os == OS.WINDOWS);
    }

    public static List<String> runCommand(String command, long timeout) throws IOException {
        return runCommand(command, false, timeout);
    }

    public static List<String> runCommand(String command) throws IOException {
        return runCommand(command, false, Settings.getSettings().getProcessTimeout());
    }

    public static List<String> runCommand(String command, boolean addErrorStream) throws IOException {
        return runCommand(command, addErrorStream, Settings.getSettings().getProcessTimeout());
    }

    public static List<String> runCommand(String command, boolean addErrorStream, long timeout) throws IOException {
        LOGGER.debug("Running command: {} with addErrorStream = {} and process timeout in sec {}", 
                command, addErrorStream, timeout);
        List<String> output = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(command);
        try {
            if (!p.waitFor(timeout, TimeUnit.SECONDS)) {
                p.destroy();
                throw new IOException("Process timed out");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // read the output from the command
        output = IOUtils.readLines(p.getInputStream(), Charset.defaultCharset());
        errorOutput = IOUtils.readLines(p.getErrorStream(), Charset.defaultCharset());
        for (String line : errorOutput) {
            LOGGER.info(line);
        }
        if (addErrorStream) {
            output.addAll(errorOutput);
        }
        return output;
    }

    public static List<String> runUnixCommand(String[] command, boolean addErrorStream) {
        LOGGER.trace("Running command: {}", Arrays.toString(command));
        ArrayList<String> output = new ArrayList<>();
        try {
            String s;
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            // read the output from the command            
            while ((s = stdInput.readLine()) != null) {
                output.add(s);
            }
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                if (addErrorStream) {
                    output.add(s);
                }

                LOGGER.trace(s);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not run the following command: {}", StringUtils.join(command));
        }
        return output;
    }

    public static String getReadPstExecutableLocation() {
        if (!hasReadpst()) {
            throw new RuntimeException("readpst is not available on this system");
        }
        return readPstExecutableLocation;
    }

    public static String getSOfficeExecutableLocation() {
        if (!hasSOffice()) {
            throw new RuntimeException("soffice is not available on this system");
        }
        return sofficeExecutableLocation;
    }

    @VisibleForTesting
    static String verifyReadpst() {
        hasReadpst = false;
        String errorMessage = "";
        if (isNix()) {
            hasReadpst = false;
            // attempt to detect where readpst is installed, even if it is not in PATH.
            String location = findExecutableLocation("readpst");
            if (verifyReadPst(location)) {
                hasReadpst = true;
                readPstExecutableLocation = location;
                LOGGER.info("Detected readpst at: " + readPstExecutableLocation);
            } else {
                LOGGER.error("Utility {} not found", READPST_VERSION);
                errorMessage = "Utility " + READPST_VERSION
                        + " is not found.\n"
                        + "It is needed to unpack *.pst mailboxes";
            }
        }
        return errorMessage;
    }

     // TODO added check for Windows
    static String verifyWkhtmltopdf() {
        String error = "";
        hasWkhtmltopdf = false;
        if (isNix()) {
            try {
                String location = findExecutableLocation("wkhtmltopdf", 
                        new String[]{"/user/local/bin", "/usr/bin"});
                if (!StringUtils.isEmpty(location)) {
                    List<String> output = OsUtil.runCommand(location + " -V");
                    if (!output.isEmpty()) {                        
                        // this works on the Mac
                        if (output.get(0).contains("wkhtmltopdf") ||
                                // and this works on Ubuntu
                                output.get(1).contains("wkhtmltopdf")) {
                            hasWkhtmltopdf = true;
                            wkhtmltopdfExecutableLocation = location;
                            LOGGER.info("Detected wkhtmltopd at: " + wkhtmltopdfExecutableLocation);
                            hasWkhtmltopdf = true;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Could not verify wkhtmltopdf");
            }
        }
        if (!hasWkhtmltopdf) {
            error = "wkhtmltopdf is not found.\n"
                    + "It is needed to convert html to PDF";
        }
        return error;
    }


    private static String findExecutableLocation(String executableName) {
        return findExecutableLocation(executableName, new String[]{});
    }

    private static String findExecutableLocation(String executableName, String[] locations) {
        String[] standartLocations = {"", "/usr/bin/", "/bin/", "/usr/sbin/", "/sbin/", "/usr/local/bin/"};
        List<String> allLocations = new ArrayList<String>();
        allLocations.addAll(Arrays.asList(standartLocations));
        allLocations.addAll(Arrays.asList(locations));
        for (String pathToExecutable : allLocations) {
            File file = new File(pathToExecutable, executableName);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    // TODO added check for Windows
    static String verifySOffice() {
        String error = "";
        hasSOffice = false;
        if (isNix()) {
            try {
                String location = findExecutableLocation("soffice", new String[]{"/Applications/LibreOffice.app/Contents/MacOS"});
                if (!StringUtils.isEmpty(location)) {
                    List<String> output = OsUtil.runCommand(location + " --version");
                    if (!output.isEmpty()) {
                        String line = output.get(0);
                        if (line.startsWith("LibreOffice")) {
                            hasSOffice = true;
                            sofficeExecutableLocation = location;
                            LOGGER.info("Detected soffice at: " + sofficeExecutableLocation);
                            hasSOffice = true;
                        }
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Could not verify soffice");
            }
        }
        if (!hasSOffice) {
            error = "LibreOffice is not found.\n"
                    + "It is needed to convert office documents to PDF";
        }
        return error;
    }

    private static boolean verifyReadPst(String readPstPath) {
        try {
            List<String> output = runCommand(readPstPath + " " + "-V");
            String versionMarker = "ReadPST / LibPST";
            String requiredVersion = READPST_VERSION;
            String error = "";
            for (String s : output) {
                if (s.startsWith(versionMarker)) {
                    if (s.compareTo(requiredVersion) < 0) {
                        error = "Required version of readpst: " + requiredVersion + " or higher";
                        LOGGER.info(error);
                    }
                    break;
                }
            }
            return error.isEmpty();
        } catch (IOException e) {
            LOGGER.trace("Unable to verify readpst at: " + readPstPath);
            return false;
        }
    }

    /**
     * Keep collecting output in buffer which can be queried from another thread
     *
     * @param command
     */
    public void runUnixCommandBuffered(String command) {
        LOGGER.trace("Running command: {}", command);
        bufferInit();
        try {
            String s;
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            // read the output from the command            
            while ((s = stdInput.readLine()) != null) {
                bufferAdd(s);
            }
            // read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                bufferAdd("ERROR: " + s);
            }
        } catch (IOException e) {
            // important enough for now, re-think logging later
            System.err.println(e.getMessage());
            bufferAdd("Could not run the following command: " + command);
        }
    }

    synchronized private void bufferInit() {
        buffer = new ArrayList<>();
    }

    synchronized private void bufferAdd(String s) {
        buffer.add(s);
    }

    synchronized public String getLastOutputLine() {
        if (buffer.size() > 0) {
            return buffer.get(buffer.size() - 1);
        } else {
            return "";
        }
    }

    /**
     * Determine file type using the Unix "file" command.
     *
     * @param filePath file to determine the type.
     * @return first line of the output of the 'file' command. Consumers will
     * have to use 'startsWith() for comparisons.
     */
    public static String getFileType(String filePath) {
        String fileType = "Unknown";
        try {
            if (isNix()) {
                List<String> output = runCommand("file " + filePath);
                if (output.isEmpty()) {
                    return "Unknown";
                } else {
                    int column = output.get(0).indexOf(": ");
                    if (column < 0) {
                        return fileType;
                    }
                    return output.get(0).substring(column + 2);
                }
            } else {
                // TODO consider using a Windows-specific tool to find file type
                if ("pst".equals(Util.getExtension(filePath))) {
                    return "Microsoft Outlook";
                }
                return fileType;
            }
        } catch (IOException e) {
            LOGGER.error("Could not verify file type");
        }
        return fileType;
    }

    public static String systemCheck() {
        StringBuilder errors = new StringBuilder();
        if (isNix()) {
            String error = verifyReadpst();
            if (!error.isEmpty()) {
                errors.append(error).append("\n\n");
            }
            error = verifyWkhtmltopdf();
            if (!error.isEmpty()) {
                errors.append(error).append("\n\n");
            }
            error = verifySOffice();
            if (!error.isEmpty()) {
                errors.append(error);
            }
        }
        return errors.toString();
    }

    public static List<String> getSystemSummary() {
        List<String> summary = new ArrayList<>();
        summary.add("readpst (PST extraction): " + hasReadpst);
        summary.add("wkhtmltopdf (html to pdf printing): " + hasWkhtmltopdf);
        summary.add("soffice (LibreOffice command line interface): " + hasSOffice);
        return summary;
    }

}
