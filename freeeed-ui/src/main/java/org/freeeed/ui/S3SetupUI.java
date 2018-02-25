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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.*;

import org.freeeed.ec2.S3Agent;
import org.freeeed.services.Settings;
import org.jets3t.service.S3ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class S3SetupUI extends JDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(S3SetupUI.class);
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;

    /**
     * Creates new form EC2SetupUI
     *  @param parent
     *
     */
    public S3SetupUI(Frame parent) {
        super(parent, true);
        initComponents();

        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(cancelName, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                doClose(RET_CANCEL);
            }
        });
    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cancelButton = new JButton();
        accessKeyIdLabel = new JLabel();
        accessKeyIdText = new JTextField();
        secretAccessKeyLabel = new JLabel();
        secretAccessKeyText = new JTextField();
        projectBucketLabel = new JLabel();
        projectBucketText = new JTextField();
        createBucketButton = new JButton();
        verifyKeysButton = new JButton();
        assignBucketButton = new JButton();
        jScrollPane1 = new JScrollPane();
        projectList = new JList();
        projectListLabel = new JLabel();
        listProjectsButton = new JButton();
        okButton = new JButton();

        setTitle("S3 Setup");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 9, 16, 24);
        getContentPane().add(cancelButton, gridBagConstraints);

        accessKeyIdLabel.setText("Access Key ID");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(29, 15, 0, 0);
        getContentPane().add(accessKeyIdLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 279;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(26, 18, 0, 24);
        getContentPane().add(accessKeyIdText, gridBagConstraints);

        secretAccessKeyLabel.setText("Secret Access Key");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(21, 15, 0, 0);
        getContentPane().add(secretAccessKeyLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 279;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 0, 24);
        getContentPane().add(secretAccessKeyText, gridBagConstraints);

        projectBucketLabel.setText("Project bucket");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(22, 15, 0, 0);
        getContentPane().add(projectBucketLabel, gridBagConstraints);

        projectBucketText.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.ipadx = 422;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 15, 0, 24);
        getContentPane().add(projectBucketText, gridBagConstraints);

        createBucketButton.setText("Create");
        createBucketButton.addActionListener(this::createBucketButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 11, 0, 24);
        getContentPane().add(createBucketButton, gridBagConstraints);

        verifyKeysButton.setText("Verify keys");
        verifyKeysButton.addActionListener(this::verifyKeysButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 15, 0, 0);
        getContentPane().add(verifyKeysButton, gridBagConstraints);

        assignBucketButton.setText("Select");
        assignBucketButton.addActionListener(this::assignBucketButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 138, 0, 0);
        getContentPane().add(assignBucketButton, gridBagConstraints);

        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(projectList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 396;
        gridBagConstraints.ipady = 206;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(9, 15, 0, 24);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        projectListLabel.setText("Projects");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(19, 15, 0, 0);
        getContentPane().add(projectListLabel, gridBagConstraints);

        listProjectsButton.setText("List");
        listProjectsButton.addActionListener(this::listProjectsButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 26, 0, 24);
        getContentPane().add(listProjectsButton, gridBagConstraints);

        okButton.setText("OK");
        okButton.setToolTipText("Select a project and start working on it");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 162, 16, 0);
        getContentPane().add(okButton, gridBagConstraints);

        pack();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void verifyKeysButtonActionPerformed(ActionEvent evt) {
        verifyKeys();
    }

    private void assignBucketButtonActionPerformed(ActionEvent evt) {
        assignBucket();
    }

    private void createBucketButtonActionPerformed(ActionEvent evt) {
        createBucket();
    }

    private void listProjectsButtonActionPerformed(ActionEvent evt) {
        listProjects();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        okButtonClicked();
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        if (returnStatus == RET_OK) {
            collectData();
        }
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(S3SetupUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /*
         * Create and display the dialog
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                S3SetupUI dialog = new S3SetupUI(new JFrame());
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    private JLabel accessKeyIdLabel;
    private JTextField accessKeyIdText;
    private JButton assignBucketButton;
    private JButton cancelButton;
    private JButton createBucketButton;
    private JScrollPane jScrollPane1;
    private JButton listProjectsButton;
    private JButton okButton;
    private JLabel projectBucketLabel;
    private JTextField projectBucketText;
    private JList projectList;
    private JLabel projectListLabel;
    private JLabel secretAccessKeyLabel;
    private JTextField secretAccessKeyText;
    private JButton verifyKeysButton;
    private int returnStatus = RET_CANCEL;

    private void showData() {
        Settings settings = Settings.getSettings();
        accessKeyIdText.setText(settings.getAccessKeyId());
        secretAccessKeyText.setText(settings.getSecretAccessKey());
        projectBucketText.setText(settings.getProjectBucket());
    }

    private void collectData() {
        Settings settings = Settings.getSettings();
        settings.setAccessKeyId(accessKeyIdText.getText());
        settings.setSecretAccessKey(secretAccessKeyText.getText());
        settings.setProjectBucket(projectBucketText.getText());
        try {
            settings.save();
        } catch (Exception e) {
            logger.error("Error saving project", e);
            JOptionPane.showMessageDialog(this, "Could not save settings " + e.getMessage());
        }

    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            showData();
        }
        super.setVisible(b);
    }

    private void verifyKeys() {
        collectData();
        S3Agent agent = new S3Agent();
        if (agent.isConnectionGood()) {
            JOptionPane.showMessageDialog(this, "Congrats! It works.");
        } else {
            JOptionPane.showMessageDialog(this, "Sorry, please check the keys.");
        }
    }

    private void assignBucket() {
        Settings settings = Settings.getSettings();
        try {
            S3Agent agent = new S3Agent();
            String[] buckets = agent.getBucketList();
            String bucket = (String) JOptionPane.showInputDialog(null,
                    "Choose your project bucket", "Select",
                    JOptionPane.INFORMATION_MESSAGE, null,
                    buckets, settings.getProjectBucket());
            if (bucket != null) {
                bucket = bucket.trim();
                projectBucketText.setText(bucket);
                settings.setProjectBucket(bucket);
            }

        } catch (S3ServiceException e) {
            JOptionPane.showMessageDialog(this, "Please verify your AWS keys");
        }
    }

    private void createBucket() {
        Settings settings = Settings.getSettings();
        String bucket = JOptionPane.showInputDialog(this, "Please choose your bucket name\nBuckets are unique across all Amazon S3");
        if (bucket == null) {
            return;
        }
        bucket = bucket.trim();
        S3Agent agent = new S3Agent();
        String createdBucket = agent.createBucket(bucket);
        if (createdBucket != null) {
            JOptionPane.showMessageDialog(this, "Bucket " + createdBucket + " was successfully created");
            projectBucketText.setText(createdBucket);
            settings.setProjectBucket(createdBucket);
        } else {
            JOptionPane.showMessageDialog(this, "Bucket names have to be unique across all Amazon S3\n"
                    + "Also, did you check your keys?");
        }
    }

    private void listProjects() {
        S3Agent agent = new S3Agent();
        String[] projects = agent.getProjectList();
        projectList.setListData(projects);
    }

    private void okButtonClicked() {
        collectData();
        String projectFileName = (String) projectList.getSelectedValue();
        if (projectFileName != null) {
            S3Agent agent = new S3Agent();
            agent.getProjectFromS3(projectFileName);
            FreeEedUI instance = FreeEedUI.getInstance();
            instance.openProject(new File(projectFileName));
        }
        doClose(RET_OK);
    }
}
