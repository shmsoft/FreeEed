package org.freeeed.services;

import java.util.ArrayList;
import org.freeeed.main.FreeEedConfiguration;

/**
 *
 * @author mark
 * Generate Enron projects for FreeEed processing
 * Use EDRM listing as input
 */
public class EnronProjects {
    public static void main(String [] argv) {
        String edrmList = "/home/mark/projects/FreeEedData/edrm_enron_list";
        String projectsDir = "/home/mark/projects/FreeEedData/projects";
    }
    public ArrayList <FreeEedConfiguration> createProjects(String edrmList) {
        ArrayList <FreeEedConfiguration> projects = new ArrayList <FreeEedConfiguration>();
        
        return projects;
    }
}
