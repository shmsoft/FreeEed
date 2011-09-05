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
 * 
 * Create web page entries
 * of this sort
 * <tr>
 * <td>1</td>
 * <td><a href="https://s3.amazonaws.com/freeeed.org/enron/results/enron001.zip">enron001.zip</a></td>
 * <td><a href="https://s3.amazonaws.com/freeeed.org/enron/results/enron001.csv">enron001.csv</a></td>
 * <td><a href="https://s3.amazonaws.com/freeeed.org/enron/results/enron001.txt">enron001.txt</a></td>
 * </tr>
 */
public class EnronProjectsPage {

    public static final DecimalFormat decimalFormat = new DecimalFormat("000");

    public static void main(String[] argv) {
        EnronProjectsPage instance = new EnronProjectsPage();
        try {
            instance.createPages();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public void createPages()
            throws IOException, ConfigurationException {
        for (int p = 1; p <= 153; ++p) {
            System.out.println("<tr>");
            System.out.println("<td>" + p + "</td>");
            System.out.println(
                    "<td><a href=\"https://s3.amazonaws.com/freeeed.org/enron/results/enron"
                    + decimalFormat.format(p) + ".zip\">enron"
                    + decimalFormat.format(p) + ".zip</a></td>");
            System.out.println(
                    "<td><a href=\"https://s3.amazonaws.com/freeeed.org/enron/results/enron"
                    + decimalFormat.format(p) + ".csv\">enron"
                    + decimalFormat.format(p) + ".csv</a></td>");
            System.out.println(
                    "<td><a href=\"https://s3.amazonaws.com/freeeed.org/enron/results/enron"
                    + decimalFormat.format(p) + ".txt\">enron"
                    + decimalFormat.format(p) + ".txt</a></td>");
            System.out.println("</tr>");
        }
    }
}
