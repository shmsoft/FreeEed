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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

public class OsUtil {

    private static final Logger logger = LoggerFactory.getLogger(OsUtil.class);
    private List<String> buffer = new ArrayList<>();
    // cached results of system check
    private static boolean readpst;
    private static boolean wkhtmltopdf;
	private static String pathToReadPst;

    /**
     * @return the readpst
     */
    public static boolean hasReadpst() {
        return readpst;
    }
    
    /**     
     * @return whether the platform has wkhtmltopdf installed
     */
    public static boolean hasWkhtmltopdf() {
        return wkhtmltopdf;
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

    /**
     * @deprecated Does not force exception handling. Use runOSCommand instead.
     */
    @Deprecated
    public static List<String> runCommand(String command) {
    	List<String> output = new ArrayList<String>();
    	try {
    		output = runCommand(command, false);
    	} catch (IOException e) {
    		logger.error("Error running command {}", command, e);
    	}
    	return output;
    }
    
    public static List<String> runOSCommand(String command) throws IOException {
    	List<String> output = runCommand(command, false);
    	return output;
    }

    public static List<String> runCommand(String command, boolean addErrorStream) throws IOException {
        logger.info("Running command: {}", command);
        List<String> output = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(command);
        // read the output from the command
        output = IOUtils.readLines(p.getInputStream(), Charset.defaultCharset());
        errorOutput = IOUtils.readLines(p.getErrorStream(), Charset.defaultCharset());
        errorOutput.forEach(line -> logger.info(line));
        if (addErrorStream) {
        	output.addAll(errorOutput);
        }
        return output;
    }
    
    public static List<String> runUnixCommand(String[] command, boolean addErrorStream) {
        logger.trace("Running command: {}", Arrays.toString(command));
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

                logger.trace(s);
            }
        } catch (IOException e) {
            logger.warn("Could not run the following command: {}", StringUtils.join(command));
        }
        return output;
    }
    
    public static String pathToReadPst() {
    	if (!hasReadpst()) {
    		throw new RuntimeException("readpst is not available on this system");
    	}
		return pathToReadPst;
	}
    
    @VisibleForTesting
    static void verifyReadpst() {
        if (isNix()) {
        	readpst = false;
        	// attempt to detect where readpst is installed, even if it is not in PATH.
        	String[] probablePathsToExecutables = { "", "/usr/bin/", "/bin/", "/usr/sbin/", "/sbin/", "/usr/local/bin/" };
        	for (String pathToExecutables : probablePathsToExecutables) {
        		String fullPath = pathToExecutables + "readpst";
        		if (verifyReadPst(fullPath)) {
        			readpst = true;
        			pathToReadPst = fullPath;
        			logger.info("Detected readpst at: " + pathToReadPst);
        			break;
        		}
        	}
        } else {
            readpst = false;            
        }
    }

    private static boolean verifyReadPst(String readPstPath) {
    	try {
            List<String> output = runOSCommand(readPstPath + " " + "-V");
            String versionMarker = "ReadPST / LibPST";
            String requiredVersion = "ReadPST / LibPST v0.6.61";
            String error = "";
            for (String s : output) {
                if (s.startsWith(versionMarker)) {
                    if (s.compareTo(requiredVersion) < 0) {
                        error = "Required version of readpst: " + requiredVersion + " or higher";
                        logger.info(error);
                    }
                    break;
                }
            }
            return error.isEmpty();
    	} catch (IOException e) {
    		logger.trace("Unable to verify readpst at: " + readPstPath);
    		return false;
    	}
	}

	public static void verifyWkhtmltopdf() {
        List<String> output = runCommand("wkhtmltopdf -V");
        wkhtmltopdf = contains(output, "wkhtmltopdf");
    }

    private static boolean contains(List <String> output, String lookForString) {
        for (String line: output) {
            if (line.contains(lookForString)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Keep collecting output in buffer which can be queried from another thread
     *
     * @param command
     */
    public void runUnixCommandBuffered(String command) {
        logger.trace("Running command: {}", command);
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
     * @return first line of the output of the 'file' command. Consumers will have to use 'startsWith() for comparisons.
     */
    public static String getFileType(String filePath) {
        String fileType = "Unknown";
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
    }

    public static void systemCheck() {
        String status;
        if (isNix()) {            
            verifyReadpst();            
            verifyWkhtmltopdf();            
        }
    }
    public static List<String> getSystemSummary() {
        List <String> summary = new ArrayList<>();
        summary.add("readpst (PST extraction): " + readpst);
        summary.add("wkhtmltopdf (html to pdf printing): " + wkhtmltopdf);
        return summary;
    }

}
