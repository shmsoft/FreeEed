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
 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freeeed.ui;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class UtilUI {

    private static final Logger logger = LoggerFactory.getLogger(UtilUI.class);

    public static void openBrowser(Component parent, String url) {
        boolean success = false;
        try {
            //if (Desktop.isDesktopSupported()) {
            Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                URI uri = new URI(url);
                desktop.browse(uri);
                success = true;
            }
            //}
        } catch (URISyntaxException | IOException e) {
            success = false;
        }
        if (!success) {
            JOptionPane.showMessageDialog(parent, "Can't open the browser - just go to\n" + url);
        }
    }

    public static void openImage(Component parent, String filePath) {
        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.open(new File(filePath));
        } catch (Exception e) {
            logger.error("Error opening image", e);
        }

    }
}
