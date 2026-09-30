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

import org.freeeed.main.FreeEedMain;
import org.freeeed.util.LogFactory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author mark
 */
public class UtilUI {

    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(UtilUI.class.getName());

    public static void openBrowser(Component parent, String url) {
        boolean success = false;
        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE) && hasBrowser()) {
                URI uri = new URI(url);
                desktop.browse(uri);
                success = true;
            }
        } catch (URISyntaxException | IOException e) {
            success = false;
        }
        if (!success) {
            JOptionPane.showMessageDialog(parent, "Can't open a browser - just go to\n" + url);
        }
    }

    /**
     * On Linux, Desktop.browse() delegates to xdg-open, which reports success
     * even when no real browser is installed; the failure then surfaces as a
     * cryptic "www-browser: No such file or directory" dialog from the child
     * process. Guard against that by checking PATH for a known browser first, so
     * we fall back to showing the URL instead. Windows and macOS always have a
     * default handler, so they are not gated.
     */
    private static boolean hasBrowser() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("linux")) {
            return true;
        }
        String path = System.getenv("PATH");
        if (path == null || path.isEmpty()) {
            return false;
        }
        String[] browsers = {"firefox", "chromium", "chromium-browser",
            "google-chrome", "google-chrome-stable", "brave-browser",
            "microsoft-edge", "epiphany-browser", "falkon", "konqueror",
            "opera", "vivaldi", "midori"};
        for (String dir : path.split(File.pathSeparator)) {
            for (String browser : browsers) {
                File f = new File(dir, browser);
                if (f.isFile() && f.canExecute()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void openImage(Component parent, String filePath) {
        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.open(new File(filePath));
        } catch (Exception e) {
            LOGGER.severe("Error opening image: " + e.getMessage());
        }

    }

    /**
     * Open the user's default email client with a pre-filled message. The user
     * remains in full control - nothing is sent until they press Send. No data
     * leaves the machine silently. Falls back to showing the address if no mail
     * client is available.
     *
     * @param parent  parent component for fallback dialogs
     * @param to      recipient address
     * @param subject email subject (will be URL-encoded)
     * @param body    email body (will be URL-encoded)
     */
    public static void openMailClient(Component parent, String to, String subject, String body) {
        String mailto = "mailto:" + to
                + "?subject=" + encodeMailParam(subject)
                + "&body=" + encodeMailParam(body);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = java.awt.Desktop.getDesktop();
                URI uri = new URI(mailto);
                if (desktop.isSupported(Desktop.Action.MAIL)) {
                    desktop.mail(uri);
                    return;
                }
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                    return;
                }
            }
        } catch (URISyntaxException | IOException | UnsupportedOperationException e) {
            LOGGER.warning("Could not open mail client: " + e.getMessage());
        }
        showMailFallback(parent, to, subject, body);
    }

    /**
     * No mail client (e.g. the headless server appliance): show the pre-filled
     * message so the user can copy it and send it from their own email.
     */
    private static void showMailFallback(Component parent, String to, String subject, String body) {
        String message = "To: " + to + "\n"
                + "Subject: " + (subject == null ? "" : subject) + "\n\n"
                + (body == null ? "" : body);
        JTextArea textArea = new JTextArea(message, 12, 50);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("<html>Could not open your email client.<br>"
                + "Please copy this message and email it to <b>" + to + "</b>:</html>"),
                BorderLayout.NORTH);
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        Object[] options = {"Copy to clipboard", "Close"};
        int choice = JOptionPane.showOptionDialog(parent, panel, "Email us",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) {
            try {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(message), null);
            } catch (IllegalStateException e) {
                LOGGER.warning("Could not copy to clipboard: " + e.getMessage());
            }
        }
    }

    /**
     * Encode a mailto query parameter. URLEncoder targets HTML forms, so we
     * convert '+' back to %20 to keep spaces correct in the mailto scheme.
     */
    private static String encodeMailParam(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            return "";
        }
    }
}
