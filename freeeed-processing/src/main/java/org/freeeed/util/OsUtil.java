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

public class OsUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(OsUtil.class);
	private static final long DEFAULT_TIMEOUT = 20000;
    private List<String> buffer = new ArrayList<>();
    // cached results of system check
    private static boolean hasReadpst;
    private static boolean hasWkhtmltopdf;
    private static boolean hasSOffice;
    private static String readPstExecutableLocation;
    private static String sofficeExecutableLocation;

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
        return runCommand(command, false, DEFAULT_TIMEOUT);
    }
    
    public static List<String> runCommand(String command, boolean addErrorStream) throws IOException {
    	return runCommand(command, addErrorStream, DEFAULT_TIMEOUT);
    }
    
    public static List<String> runCommand(String command, boolean addErrorStream, long timeout) throws IOException {
        logger.info("Running command: {}", command);
        List<String> output = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();
        Process p = Runtime.getRuntime().exec(command);
        try {
	        if (!p.waitFor(timeout, TimeUnit.MILLISECONDS)) {
	        	p.destroy();
	        	throw new IOException("Process timed out");
	        }
        } catch (InterruptedException e) {
        	throw new RuntimeException(e);
        }
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
    static void verifyReadpst() {
        if (isNix()) {
            hasReadpst = false;
            // attempt to detect where readpst is installed, even if it is not in PATH.
            String location = findExecutableLocation("readpst");
            if (verifyReadPst(location)) {
            	hasReadpst = true;
            	readPstExecutableLocation = location;
            	logger.info("Detected readpst at: " + readPstExecutableLocation);
            }
        } else {
        	hasReadpst = false;
        }
    }
    
    private static String findExecutableLocation(String executableName) {
    	return findExecutableLocation(executableName, new String[] {} );
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

	static void verifySOffice() {
    	if (isNix()) {
    		try {
    			String location = findExecutableLocation("soffice", new String[] { "/Applications/LibreOffice.app/Contents/MacOS" } );
    			if (!StringUtils.isEmpty(location)) {
    				List<String> output = OsUtil.runCommand(location + " --version");
    				if (!output.isEmpty()) {
    					String line = output.get(0);
    					if (line.startsWith("LibreOffice")) {
    						hasSOffice = true;
    						sofficeExecutableLocation = location;
    						logger.error("Detected soffice at: " + sofficeExecutableLocation);
    					}
    				}
    			}
    		} catch (IOException e) {
    			logger.error("Could not verify soffice", e);
    		}
    	} else {
    		hasSOffice = false;
    	}
    }
    
    private static boolean verifyReadPst(String readPstPath) {
        try {
            List<String> output = runCommand(readPstPath + " " + "-V");
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
        try {
            List<String> output = runCommand("wkhtmltopdf -V");
            hasWkhtmltopdf = contains(output, "wkhtmltopdf");
        } catch (IOException e) {
            logger.error("Problem verifying wkhtmltopdf", e);
        }
    }
    
    private static boolean contains(List<String> output, String lookForString) {
        for (String line : output) {
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
            logger.error("Could not verify file type", e);
        }
        return fileType;
    }
    
    public static void systemCheck() {
        String status;
        if (isNix()) {
            verifyReadpst();
            verifyWkhtmltopdf();
            verifySOffice();
        }
    }
    
    public static List<String> getSystemSummary() {
        List<String> summary = new ArrayList<>();
        summary.add("readpst (PST extraction): " + hasReadpst);
        summary.add("wkhtmltopdf (html to pdf printing): " + hasWkhtmltopdf);
        summary.add("soffice (LibreOffice command line interface): " + hasSOffice);
        return summary;
    }
    
}
