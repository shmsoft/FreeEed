package org.freeeed.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.freeeed.util.History;

public class LinuxUtil {

    public static List <String> runLinuxCommand(String command) {
		History.appendToHistory("Running command: " + command);
        ArrayList <String> output = new ArrayList <String> ();
        String s = null;
        try {
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
            History.appendToHistory("Could not run the following command:");
            History.appendToHistory(command);
        }
        return output;
    }
    public static String verifyReadpst() {
        List <String> output = runLinuxCommand("readpst -V");
        String pstVersion = "ReadPST / LibPST v0.6.";
        String error = "Expected V 0.6.41 of readpst or higher\n" +
                "You can install it on Ubuntu with the following command:\n" +
                "sudo apt-get install readpst";
        for (String s: output) {
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
}
