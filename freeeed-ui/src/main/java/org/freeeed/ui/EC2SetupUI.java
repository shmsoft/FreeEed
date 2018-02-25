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
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

import org.freeeed.services.Settings;

import static java.awt.GridBagConstraints.*;

/**
 *
 * @author mark
 */
public class EC2SetupUI extends JDialog {
    private Frame parent;
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    private String[] instanceTypes = {"c1.medium", "c1.xlarge"};
    private String[] availabilityZones = {"us-east-1a",
        "us-east-1b", "us-east-1c", "us-east-1d", "eu-west-1c"};

    /**
     * Creates new form EC2SetupUI
     * @param parent
     *
     */
    public EC2SetupUI(final Frame parent) {
        super(parent, true);
        this.parent = parent;
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
        GridBagConstraints gridBagConstraints;

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        securityGroupLabel = new javax.swing.JLabel();
        securityGroupText = new javax.swing.JTextField();
        keyPairNameLabel = new javax.swing.JLabel();
        keyPairNameText = new javax.swing.JTextField();
        pemCertificateLabel = new javax.swing.JLabel();
        showPemButton = new javax.swing.JButton();
        clusterSizeLabel = new javax.swing.JLabel();
        clusterSizeText = new javax.swing.JTextField();
        setupTimeoutLabel = new javax.swing.JLabel();
        setupTimeoutText = new javax.swing.JTextField();
        instanceTypeLabel = new javax.swing.JLabel();
        instanceTypeChoice = new javax.swing.JComboBox();
        availabilityZoneLabel = new javax.swing.JLabel();
        availabilityZoneChoice = new javax.swing.JComboBox();
        skipInstanceCreation = new javax.swing.JCheckBox();

        setTitle("EC2 setup");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        okButton.setText("OK");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.ipadx = 26;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 115, 16, 0);
        getContentPane().add(okButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 8, 16, 0);
        getContentPane().add(cancelButton, gridBagConstraints);

        securityGroupLabel.setText("Security group");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 21, 0, 0);
        getContentPane().add(securityGroupLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 260;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(16, 36, 0, 0);
        getContentPane().add(securityGroupText, gridBagConstraints);

        keyPairNameLabel.setText("Key pair name");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 21, 0, 0);
        getContentPane().add(keyPairNameLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 260;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(16, 36, 0, 0);
        getContentPane().add(keyPairNameText, gridBagConstraints);

        pemCertificateLabel.setText("PEM certificate");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(13, 21, 0, 0);
        getContentPane().add(pemCertificateLabel, gridBagConstraints);

        showPemButton.setText("Show");
        showPemButton.addActionListener(this::showPemButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(9, 36, 0, 0);
        getContentPane().add(showPemButton, gridBagConstraints);

        clusterSizeLabel.setText("Cluster size");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(21, 21, 0, 0);
        getContentPane().add(clusterSizeLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 61;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(18, 36, 0, 0);
        getContentPane().add(clusterSizeText, gridBagConstraints);

        setupTimeoutLabel.setText("Setup timeout");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 21, 0, 0);
        getContentPane().add(setupTimeoutLabel, gridBagConstraints);

        setupTimeoutText.setToolTipText("<html>If a cluster fails to start up in this many minutes, <br />\nthere may be a problem with EC2 - abandon the attempt.</html.");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 61;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(16, 36, 0, 0);
        getContentPane().add(setupTimeoutText, gridBagConstraints);

        instanceTypeLabel.setText("Instance type");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(14, 21, 0, 0);
        getContentPane().add(instanceTypeLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 230;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(11, 36, 0, 0);
        getContentPane().add(instanceTypeChoice, gridBagConstraints);

        availabilityZoneLabel.setText("Availability zone");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(19, 21, 0, 0);
        getContentPane().add(availabilityZoneLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 230;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(16, 36, 0, 0);
        getContentPane().add(availabilityZoneChoice, gridBagConstraints);

        skipInstanceCreation.setSelected(true);
        skipInstanceCreation.setText("Skip instance creation (use already started machines, usually spot instances)");
        skipInstanceCreation.setHorizontalTextPosition(SwingConstants.LEFT);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.anchor = NORTHWEST;
        gridBagConstraints.insets = new Insets(51, 21, 0, 11);
        getContentPane().add(skipInstanceCreation, gridBagConstraints);

        pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (!collectData()) {
            return;
        }
        doClose(RET_OK);
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

    private void showPemButtonActionPerformed(ActionEvent evt) {
        PemUI ui = new PemUI(parent);
        ui.setVisible(true);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    private JComboBox availabilityZoneChoice;
    private JLabel availabilityZoneLabel;
    private JButton cancelButton;
    private JLabel clusterSizeLabel;
    private JTextField clusterSizeText;
    private JComboBox instanceTypeChoice;
    private JLabel instanceTypeLabel;
    private JLabel keyPairNameLabel;
    private JTextField keyPairNameText;
    private JButton okButton;
    private JLabel pemCertificateLabel;
    private JLabel securityGroupLabel;
    private JTextField securityGroupText;
    private JLabel setupTimeoutLabel;
    private JTextField setupTimeoutText;
    private JButton showPemButton;
    private JCheckBox skipInstanceCreation;
    private int returnStatus = RET_CANCEL;

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            showData();
        }
        super.setVisible(visible);
    }

    private void showData() {
        Settings settings = Settings.getSettings();
        securityGroupText.setText(settings.getSecurityGroup());
        keyPairNameText.setText(settings.getKeyPair());
        clusterSizeText.setText(Integer.toString(settings.getClusterSize()));
        setupTimeoutText.setText(Integer.toString(settings.getClusterTimeoutMin()));
        instanceTypeChoice.setModel(new DefaultComboBoxModel(instanceTypes));
        instanceTypeChoice.setSelectedItem(settings.getInstanceType());
        availabilityZoneChoice.setModel(new DefaultComboBoxModel(availabilityZones));
        availabilityZoneChoice.setSelectedItem(settings.getAvailabilityZone());
        skipInstanceCreation.setSelected(settings.skipInstanceCreation());
    }

    private boolean collectData() {
        Settings settings = Settings.getSettings();
        String mes = "Please enter a valid cluster size";
        try {
            int clusterSize = Integer.parseInt(clusterSizeText.getText().trim());
            if (clusterSize < 1) {
                JOptionPane.showMessageDialog(rootPane, mes);
                return false;
            }
            settings.setClusterSize(clusterSize);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(rootPane, mes + " " + e.getMessage());
            return false;
        }
        mes = "Please enter a valid cluster timetout in minutes";
        try {
            int clusterTimetout = Integer.parseInt(setupTimeoutText.getText().trim());
            if (clusterTimetout < 1) {
                JOptionPane.showMessageDialog(rootPane, mes);
                return false;
            }
            settings.setClusterTimeoutMin(clusterTimetout);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(rootPane, mes + " " + e.getMessage());
            return false;
        }

        settings.setSkipInstanceCreation(skipInstanceCreation.isSelected());
        settings.setSecurityGroup(securityGroupText.getText().trim());
        settings.setKeyPair(keyPairNameText.getText());
        settings.setInstanceType((String) instanceTypeChoice.getSelectedItem());
        settings.setAvailabilityZone((String) availabilityZoneChoice.getSelectedItem());
        return true;
    }
}
