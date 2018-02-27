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

import org.freeeed.helpers.StagingProgressUIHelper;
import org.freeeed.main.ActionStaging;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.BorderFactory.createTitledBorder;

/**
 * @author ivanl
 */
public class StagingProgressUI extends javax.swing.JDialog implements StagingProgressUIHelper {

    private static final Logger logger = LoggerFactory.getLogger(StagingProgressUI.class);

    private boolean stagingFinished = false;
    private long total = 1;
    private long currentSize = 0;
    private final ActionStaging staging;
    private Thread stagingThread;
    private JButton cancelButton;
    private JLabel fileLabel;
    private JPanel jPanel1;
    private JButton okButton;
    private JLabel operationLabel;
    private JProgressBar progressBar;
    private JCheckBox straightThroughCheck;

    /**
     * Creates new form StagingProgressUI
     *
     * @param parent
     * @param modal
     */
    public StagingProgressUI(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        myInitComponents();
        staging = new ActionStaging(this);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        jPanel1 = new JPanel();
        progressBar = new JProgressBar();
        operationLabel = new JLabel();
        fileLabel = new JLabel();
        okButton = new JButton();
        cancelButton = new JButton();
        straightThroughCheck = new JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Staging");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(1100, 200));

        jPanel1.setBorder(createTitledBorder(createTitledBorder("Staging")));
        jPanel1.setLayout(new GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 1000;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(15, 21, 0, 0);
        jPanel1.add(progressBar, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 94;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 21, 0, 0);
        jPanel1.add(operationLabel, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 495;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 21, 23, 0);
        jPanel1.add(fileLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 36;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(29, 15, 0, 15);
        getContentPane().add(jPanel1, gridBagConstraints);

        okButton.setText("Ok");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(18, 56, 44, 0);
        getContentPane().add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 18;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(18, 9, 44, 15);
        getContentPane().add(cancelButton, gridBagConstraints);

        straightThroughCheck.setText("Continue with local processing after staging");
        straightThroughCheck.addActionListener(this::straightThroughCheckActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(18, 15, 44, 0);
        getContentPane().add(straightThroughCheck, gridBagConstraints);

        pack();
    }

    private void formWindowClosing(WindowEvent evt) {
        cancelStaging();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        cancelStaging();
    }

    private void straightThroughCheckActionPerformed(java.awt.event.ActionEvent evt) {
        setStraightThroughProcessing();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            startStaging();
            centerWindow(this);
        }

        super.setVisible(visible);
    }

    private void cancelStaging() {
        if (!stagingFinished) {
            int confirm = JOptionPane.showConfirmDialog(this, "Please confirm cancel staging");
            if (confirm == JOptionPane.OK_OPTION) {
                staging.setInterrupted();
                try {
                    stagingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
        doClose();
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private void myInitComponents() {
        okButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        straightThroughCheck.setSelected(Settings.getSettings().isStraightThroughProcessing());
    }

    public void setStagingFinished() {
        this.stagingFinished = true;
    }

    /**
     * Callback for progress update
     */
    @Override
    public void setDownloadingState() {
        EventQueue.invokeLater(() -> operationLabel.setText("Downloading..."));
    }

    /**
     * Callback for progress update
     */
    public void setPackagingState() {
        EventQueue.invokeLater(() -> operationLabel.setText("Packaging..."));
    }

    /**
     * Callback for progress update
     */
    @Override
    public void setPreparingState() {
        EventQueue.invokeLater(() -> operationLabel.setText("Preparing..."));
    }

    /**
     * Callback for progress update
     */
    @Override
    public synchronized void setDone() {
        EventQueue.invokeLater(() -> {
            operationLabel.setText("Done");
            stagingFinished = true;
            okButton.setEnabled(true);
            progressBar.setValue(100);
            if (Settings.getSettings().isStraightThroughProcessing()) {
                String runWhere = "local";
                Project.getCurrentProject().setEnvironment(runWhere);
                try {
                    doClose();
                    FreeEedUI.getInstance().processProject();
                } catch (Exception e) {
                    logger.error("Problem processing after staging", e);
                }
            }
        });
    }

    /**
     * Callback for progress update
     *
     * @param total total progress
     */
    @Override
    public void setTotalSize(long total) {
        this.total = total;
    }

    @Override
    public void resetCurrentSize() {
        EventQueue.invokeLater(() -> {
            currentSize = 0;
            progressBar.setValue(0);
        });
    }

    /**
     * Callback for progress update
     *
     * @param fileName
     */
    @Override
    public void updateProcessingFile(final String fileName) {
        final String displayName = (fileName.length() <= 72) ? fileName : "..." + fileName.substring(fileName.length() - 72);
        EventQueue.invokeLater(() -> fileLabel.setText(displayName));
    }

    /**
     * Callback for progress update
     *
     * @param size of files copied so far
     */
    @Override
    public void updateProgress(long size) {
        if (size > 0) {
            currentSize += size;
            final long value = currentSize * 100 / total;
            EventQueue.invokeLater(() -> progressBar.setValue((int) value));
        }
    }

    /**
     * Callback for progress update
     */
    public void startStaging() {
        stagingThread = new Thread(staging);
        stagingThread.start();
    }

    public static void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private void setStraightThroughProcessing() {
        boolean b = straightThroughCheck.isSelected();
        Settings.getSettings().setStraighThroughProcessing(b);
        try {
            Settings.getSettings().save();
        } catch (Exception e) {
            logger.error("Could not save settings", e);
        }
    }
}
