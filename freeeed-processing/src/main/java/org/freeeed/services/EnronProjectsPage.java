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
package org.freeeed.services;

import java.io.IOException;
import java.text.DecimalFormat;
import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author mark
 * 
 * Create web page entries
 * of this sort
 *      enron001.zip (with link)
 *      enron001.csv (with link)
 *      enron001.txt (with link)
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
