package org.freeeed.services;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.freeeed.main.FreeEedConfiguration;
import org.freeeed.main.ParameterProcessing;

/**
 *
 * @author mark
 * Generate Enron projects for FreeEed processing
 * Use EDRM listing as input
 */
public class EnronProjects {

    public static final DecimalFormat decimalFormat = new DecimalFormat("000");

    public static void main(String[] argv) {
        String edrmList = "/home/mark/projects/FreeEedData/edrm_enron_list";
        String projectsDir = "/home/mark/projects/FreeEedData/projects";
        EnronProjects instance = new EnronProjects();
        try {
            instance.createProjects(edrmList, projectsDir);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public void createProjects(String edrmList, String projectDir)
            throws IOException, ConfigurationException {
        if (new File(projectDir).exists()) {
            Files.deleteRecursively(new File(projectDir));
        }
        List<String> lines = Files.readLines(new File(edrmList), Charset.defaultCharset());
        String marker = "http://duaj3yp6waei2.cloudfront.net/";
        int projectNumber = 0;
        for (String line : lines) {
            int markerIndex = line.indexOf(marker);
            if (markerIndex < 0) {
                continue;
            }
            int markerEnd = line.indexOf("\"", markerIndex);
            if (markerEnd < 0) {
                continue;
            }
            String fileName = line.substring(markerIndex + marker.length(), markerEnd);
            System.out.println(fileName);
            String startFile = "edrm-enron-v2_";
            String endFile = "_pst.zip";
            String custodianFirstName =
                    fileName.substring(startFile.length(),
                    fileName.length() - endFile.length() - 2);
            String custodianLastName =
                    fileName.substring(fileName.length() - endFile.length() - 1,
                    fileName.length() - endFile.length());
            System.out.println(custodianFirstName + " " + custodianLastName);

            FreeEedConfiguration project = new FreeEedConfiguration();
            ++projectNumber;
            project.setProperty(ParameterProcessing.GIGS_PER_ZIP_STAGING, 1.);
            // TODO this is not used, fix!
            project.setProperty("output-dir", "test-output/output");
            project.setProperty("staging-dir", "test-output/staging");
            project.setProperty(ParameterProcessing.STAGE, "");
            project.setProperty(ParameterProcessing.CULLING, "");
            project.setProperty(ParameterProcessing.PROCESS_WHERE, "local");
            project.setProperty(ParameterProcessing.PROJECT_INPUTS,
                    line.substring(markerIndex, markerEnd));
            project.setProperty(ParameterProcessing.PROJECT_CUSTODIANS,
                    capitalize(custodianFirstName) + " " + 
                    capitalize(custodianLastName));
            project.setProperty(ParameterProcessing.PROJECT_NAME,
                    "Enron " + decimalFormat.format(projectNumber)
                    + " with remote data access");
            String projectFileName = projectDir + "/"
                    + "enron" + decimalFormat.format(projectNumber) + ".project";
            project.save(new File(projectFileName));
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase()
                + str.substring(1, str.length());
    }
}
