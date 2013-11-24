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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.freeeed.services.History;

public class PlatformUtil {
    private static final Logger log = Logger.getLogger(PlatformUtil.class);
    
    private List<String> buffer = new ArrayList<String>();

    public static enum PLATFORM {

        LINUX, WINDOWS, MACOSX, UNKNOWN
    };

    public static PLATFORM getPlatform() {
        String platform = System.getProperty("os.name").toLowerCase();
        if (platform.startsWith("windows")) {
            return PLATFORM.WINDOWS;
        } else if (platform.startsWith("linux")) {
            return PLATFORM.LINUX;
        } else if (platform.startsWith("mac os x")) {
            return PLATFORM.MACOSX;
        } else {
            return PLATFORM.UNKNOWN;
        }
    }

    public static List<String> runUnixCommand(String command) {
    	return runUnixCommand(command, false);
    }
    
    public static List<String> runUnixCommand(String command, boolean addErrorStream) {
        log.debug("Running command: " + command);
        
        History.appendToHistory("Running command: " + command);
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
            	
                History.appendToHistory(s);
            }
        } catch (IOException e) {
            // important enough for now, re-think logging later
            System.err.println(e.getMessage());
            History.appendToHistory("Could not run the following command:");
            History.appendToHistory(command);
        }
        return output;
    }
    
    public static String verifyReadpst() {
        List<String> output = runUnixCommand("readpst -V");
        String pstVersion = "ReadPST / LibPST v0.6.";
        String error = "Expected V 0.6.41 of readpst or higher\n"
                + "You can install it on Ubuntu with the following command:\n"
                + "sudo apt-get install readpst";
        for (String s : output) {
            if (s.startsWith(pstVersion)) {
                int v = Integer.parseInt(s.substring(pstVersion.length()));
                if (v >= 41) {
                    error = null;
                }
                break;
            }
        }
        return error;
    }

    public static String verifyWkhtmltopdf() {
        List<String> output = runUnixCommand("wkhtmltopdf -V");
        String error = "Expected wkhtmltopdf\n"
                + "You can install it on Ubuntu with the following command:\n"
                + "sudo apt-get install wkhtmltopdf";
        if (output.size() > 0) {
            error = null;
        }
        return error;
    }

    /**
     * Keep collecting output in buffer which can be queried from another thread
     *
     * @param command
     */
    public void runUnixCommandBuffered(String command) {
        History.appendToHistory("Running command: " + command);
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
        buffer = new ArrayList<String>();
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
}
