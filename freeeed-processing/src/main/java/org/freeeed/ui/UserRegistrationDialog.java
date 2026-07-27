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
package org.freeeed.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.Version;
import org.freeeed.services.Activation;
import org.freeeed.services.Settings;

/**
 * Soft, free registration. FreeEed is free to RUN and ungated - we only ask the
 * user for a name and email so we can send fixes/updates and they can reach us
 * (issue #549). An activation key is NOT required to run the app; it is reserved
 * for gating updates later (see {@link org.freeeed.services.Activation}).
 *
 * Flow:
 *   1. The user enters Name / Company / Email and clicks "Continue" - the app
 *      opens immediately. No key, no waiting on a reply.
 *   2. (Optional) They can click "Email my registration" to say hello and get on
 *      our update list.
 *
 * Closing the dialog (or clicking "Quit") exits the program; clicking "Continue"
 * (with name + email) enters it. Existing users who already have a valid key stay
 * activated automatically.
 *
 * @author mark
 */
public class UserRegistrationDialog extends JDialog {

    private JTextField nameField;
    private JTextField companyField;
    private JTextField emailField;
    private JTextArea projectArea;
    private JTextField keyField;

    private boolean activated = false;

    public UserRegistrationDialog(Frame parent) {
        super(parent, true); // always modal
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // closing = quit, handled below
        initComponents();
        setTitle("Activate FreeEed - free registration");
        setLocationRelativeTo(parent);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                quit();
            }
        });
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        content.add(buildMessagePanel(), BorderLayout.NORTH);
        content.add(buildFormPanel(), BorderLayout.CENTER);
        content.add(buildButtonPanel(), BorderLayout.SOUTH);

        setContentPane(content);
        pack();
        setMinimumSize(getSize());
    }

    private JComponent buildMessagePanel() {
        JLabel heading = new JLabel("Welcome - let's get you started (it's free)");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, heading.getFont().getSize() + 4f));

        JTextArea message = new JTextArea(
                "FreeEed is free to use. We just ask who you are so we can send you bug "
                + "fixes, security patches and updates - and so you can reach us when "
                + "you need help.\n\n"
                + "1. Enter your name and email.\n"
                + "2. Click \"Continue\" to start FreeEed - that's it, no key needed.\n"
                + "3. (Optional) Click \"Email my registration\" to say hello and get on "
                + "our update list.\n\n"
                + "We never collect your case data. We care about our users, not profit - "
                + "this is how we stay in touch.");
        message.setEditable(false);
        message.setOpaque(false);
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        message.setFont(UIManager.getFont("Label.font"));
        message.setColumns(44);
        message.setRows(10);

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(heading, BorderLayout.NORTH);
        panel.add(message, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        nameField = new JTextField(24);
        companyField = new JTextField(24);
        emailField = new JTextField(24);
        projectArea = new JTextArea(3, 24);
        projectArea.setLineWrap(true);
        projectArea.setWrapStyleWord(true);
        keyField = new JTextField(24);

        // Pre-fill if the user registered before but hasn't activated yet.
        Settings settings = Settings.getSettings();
        nameField.setText(settings.getUserName());
        companyField.setText(settings.getUserCompany());
        emailField.setText(settings.getUserEmail());
        projectArea.setText(settings.getUserProject());
        keyField.setText(settings.getActivationKey());

        addRow(form, c, 0, "Name:", nameField);
        addRow(form, c, 1, "Company:", companyField);
        addRow(form, c, 2, "Email:", emailField);

        // Project prompt spans the full width, with the text area beneath it.
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(new JLabel("Tell us a few words about your project:"), c);

        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        form.add(new JScrollPane(projectArea), c);
        c.gridwidth = 1;
        c.weighty = 0;

        addRow(form, c, 5, "Activation key (optional):", keyField);

        return form;
    }

    private void addRow(JPanel form, GridBagConstraints c, int row, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = row;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, c);
    }

    private JComponent buildButtonPanel() {
        JButton emailButton = new JButton("Email my registration (optional)");
        emailButton.addActionListener(e -> emailRegistration());

        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> continueToApp());

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> quit());

        getRootPane().setDefaultButton(continueButton);

        JPanel panel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        panel.add(quitButton);
        panel.add(emailButton);
        panel.add(continueButton);
        return panel;
    }

    /** Step 1 + 2: save the details and open the user's mail client to us. */
    private void emailRegistration() {
        String name = nameField.getText().trim();
        String company = companyField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least your name and email first.",
                    "A little more, please", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "That email address doesn't look right. Please check it.",
                    "Check email", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String project = projectArea.getText().trim();

        // Persist details now so we can pre-fill next launch while they await the key.
        Settings settings = Settings.getSettings();
        settings.setUserName(name);
        settings.setUserCompany(company);
        settings.setUserEmail(email);
        settings.setUserProject(project);
        settings.setRegistrationStatus(ParameterProcessing.REGISTRATION_REGISTERED);
        saveQuietly(settings);

        String subject = "FreeEed registration: " + name;
        String body = "Hi FreeEed team,\n\n"
                + "Registering my FreeEed install - please add me to your update list.\n\n"
                + "Name: " + name + "\n"
                + "Company: " + company + "\n"
                + "Email: " + email + "\n"
                + "About my project: " + (project.isEmpty() ? "(not provided)" : project) + "\n"
                + "Version: " + Version.getVersionAndBuild() + "\n"
                + "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + "\n";
        UtilUI.openMailClient(this, ParameterProcessing.SUPPORT_EMAIL, subject, body);

        JOptionPane.showMessageDialog(this,
                "Thank you! Your email client should be opening - just press Send.\n"
                + "You're all set - click \"Continue\" to start using FreeEed.",
                "Registration sent", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Enter FreeEed. FreeEed is free and ungated - all we ask is your name and
     * email so we can send updates and you can reach us. No activation key is
     * required to run the app. If the user happens to have pasted a valid key
     * (for update entitlement), we keep it, but it is never required here.
     */
    private void continueToApp() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter at least your name and email so we can keep you posted.",
                    "A little more, please", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "That email address doesn't look right. Please check it.",
                    "Check email", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Settings settings = Settings.getSettings();
        settings.setUserName(name);
        settings.setUserCompany(companyField.getText().trim());
        settings.setUserEmail(email);
        settings.setUserProject(projectArea.getText().trim());
        // Activation key is optional now (reserved for gating updates); store it
        // only if the user pasted a valid one - never block on it.
        String key = keyField.getText().trim();
        if (Activation.isValid(email, key)) {
            settings.setActivationKey(key);
        }
        settings.setRegistrationStatus(ParameterProcessing.REGISTRATION_REGISTERED);
        saveQuietly(settings);

        activated = true;
        dispose();
    }

    private void quit() {
        activated = false;
        dispose();
    }

    private static void saveQuietly(Settings settings) {
        try {
            settings.save();
        } catch (Exception ex) {
            // Non-fatal; in-memory settings still hold for this session.
        }
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    /**
     * Hard activation gate. Returns immediately if this install is already
     * activated; otherwise shows the modal dialog and blocks until the user
     * activates or quits.
     *
     * @param parent the parent frame (may be null at startup)
     * @return true if the application may proceed, false if the user quit.
     */
    public static boolean ensureActivated(Frame parent) {
        try {
            if (Settings.getSettings().isActivated()) {
                return true;
            }
            UserRegistrationDialog dialog = new UserRegistrationDialog(parent);
            dialog.setVisible(true); // blocks (modal)
            return dialog.activated;
        } catch (Exception e) {
            // Fail open: never let a bug in the gate brick the application.
            return true;
        }
    }
}
