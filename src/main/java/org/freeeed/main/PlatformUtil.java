package org.freeeed.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.freeeed.services.History;

public class PlatformUtil {

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
        History.appendToHistory("Running command: " + command);
        ArrayList<String> output = new ArrayList<String>();        
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
        if (output.size() > 0) error = null;
        return error;
    }    
}
