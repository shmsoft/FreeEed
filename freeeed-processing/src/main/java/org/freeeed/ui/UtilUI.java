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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
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
        // Linux fallback: under non-GNOME window managers (e.g. a minimal openbox appliance),
        // Desktop.isSupported(BROWSE) is false even when a browser is installed, so the block above
        // never fires. Try xdg-open (honors the system default handler); if that doesn't open a
        // browser -- no handler registered, or it "succeeds" without launching -- launch the first
        // browser found on PATH directly, the reliable path on a bare desktop / the appliance.
        if (!success && isLinux()) {
            // Only try xdg-open when a default http handler is actually registered. xdg-open can
            // exit 0 without opening anything (no handler), which would look like success and skip
            // the direct launch -- Review would then silently do nothing. On the appliance the
            // handler is empty, so this goes straight to the direct launch.
            if (hasDefaultHttpHandler()) {
                success = xdgOpen(url);
            }
            if (!success) {
                success = launchBrowserDirect(url);
            }
        }
        if (!success) {
            JOptionPane.showMessageDialog(parent,
                    "Can't open a browser automatically. Please browse to:\n" + browseHint(url));
        }
    }

    private static boolean isLinux() {
        return System.getProperty("os.name", "").toLowerCase().contains("linux");
    }

    /** True if a default handler is registered for http URLs (xdg-mime). Gates xdgOpen() so we never
     *  call xdg-open when it would exit 0 without opening anything. Short timeout: runs on the EDT. */
    private static boolean hasDefaultHttpHandler() {
        try {
            Process p = new ProcessBuilder("xdg-mime", "query", "default", "x-scheme-handler/http")
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!p.waitFor(3, TimeUnit.SECONDS)) {
                p.destroy();
                return false;
            }
            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return p.exitValue() == 0 && !out.isEmpty();
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /** Open a URL via xdg-open (the system default handler). Called only when a default handler
     *  exists. Runs on the Swing EDT, so wait only briefly: if xdg-open stays attached to the browser
     *  it launched (generic mode), treat "still running" as success rather than freezing the UI or
     *  falling through to a second browser. */
    private static boolean xdgOpen(String url) {
        try {
            Process p = new ProcessBuilder("xdg-open", url)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            if (!p.waitFor(3, TimeUnit.SECONDS)) {
                return true; // still running => it launched a browser and stayed attached
            }
            return p.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            LOGGER.warning("xdg-open failed: " + e.getMessage());
            return false;
        }
    }

    /** For a localhost URL, also show the machine's LAN address, which is what users on other
     *  computers need (the appliance is reached at http://&lt;vm-ip&gt;:8090/freeeedui). */
    private static String browseHint(String url) {
        try {
            if (url.contains("localhost") || url.contains("127.0.0.1")) {
                String ip = firstLanIp();
                if (ip != null) {
                    return url + "\n\nFrom another computer on the network, use:\n"
                            + url.replaceFirst("localhost|127\\.0\\.0\\.1", ip);
                }
            }
        } catch (Exception ignore) {
            // fall through to the plain URL
        }
        return url;
    }

    /** First site-local IPv4 address of a live, non-loopback interface, or null. */
    private static String firstLanIp() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignore) {
            // no usable address
        }
        return null;
    }

    /** Browsers we know how to launch directly, checked in this order. */
    private static final String[] LINUX_BROWSERS = {"firefox", "firefox-esr", "firefox-bin",
        "chromium", "chromium-browser", "google-chrome", "google-chrome-stable", "brave-browser",
        "microsoft-edge", "epiphany-browser", "falkon", "konqueror", "opera", "vivaldi", "midori"};

    /** The first browser from LINUX_BROWSERS found executable on PATH, or null. */
    private static File findBrowserOnPath() {
        String path = System.getenv("PATH");
        if (path == null || path.isEmpty()) {
            return null;
        }
        for (String dir : path.split(File.pathSeparator)) {
            for (String browser : LINUX_BROWSERS) {
                File f = new File(dir, browser);
                if (f.isFile() && f.canExecute()) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * On Linux, Desktop.browse() delegates to xdg-open, which reports success even when no real
     * browser is installed; the failure then surfaces as a cryptic "www-browser: No such file or
     * directory" dialog. Guard by checking PATH for a known browser first. Windows and macOS always
     * have a default handler, so they are not gated.
     */
    private static boolean hasBrowser() {
        if (!isLinux()) {
            return true;
        }
        return findBrowserOnPath() != null;
    }

    /** Launch the first browser found on PATH directly with the URL -- the reliable fallback when
     *  neither Desktop.browse nor xdg-open opens anything (e.g. a minimal WM with no default handler,
     *  as on the appliance). */
    private static boolean launchBrowserDirect(String url) {
        File browser = findBrowserOnPath();
        if (browser == null) {
            return false;
        }
        try {
            new ProcessBuilder(browser.getAbsolutePath(), url)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            return true;
        } catch (IOException e) {
            LOGGER.warning("direct browser launch failed: " + e.getMessage());
            return false;
        }
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
