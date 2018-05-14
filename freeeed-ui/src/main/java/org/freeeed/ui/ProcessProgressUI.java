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

import org.freeeed.helpers.ProcessProgressUIHelper;
import org.freeeed.helpers.ProgressBar;
import org.freeeed.main.ActionProcessing;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * @author ivanl
 */
public class ProcessProgressUI extends javax.swing.JDialog implements ProcessProgressUIHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessProgressUI.class);
    private boolean processingFinished = false;
    private long total = 1;
    private final ActionProcessing processing;
    private Thread processingThread;

    /**
     * instance is used only when it exists. For Hadoop server-based processing
     * it will be null
     */
    private static ProcessProgressUI instance;

    public synchronized ProcessProgressUI getInstance() {
        return instance;
    }

    /**
     * Creates new form StagingProgressUI
     *
     * @param parent
     * @param modal
     */
    public ProcessProgressUI(Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        myInitComponents();
        processing = new ActionProcessing(this);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        fileLabel = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Processing");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Staging"), "Processing progress"));
        jPanel1.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 800;
        gridBagConstraints.ipady = 8;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 23, 0, 23);
        jPanel1.add(progressBar, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 23, 25, 23);
        jPanel1.add(fileLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 2;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(29, 15, 0, 15);
        getContentPane().add(jPanel1, gridBagConstraints);

        okButton.setText("Ok");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 17;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 376, 40, 0);
        getContentPane().add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 45;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 18, 40, 15);
        getContentPane().add(cancelButton, gridBagConstraints);

        pack();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        cancelProcessing();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        doClose();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        cancelProcessing();
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            Project.getCurrentProject().setStopThePresses(false);
            startProcessing();
            centerWindow(this);
            instance = this;
            ProgressBar.initialize(this);
        }

        super.setVisible(visible);
    }

    private void cancelProcessing() {
        if (processingFinished) {
            return;
        }
        EventQueue.invokeLater(() -> {
            int confirm = JOptionPane.showConfirmDialog(null, "Please confirm cancel processing");
            if (confirm == JOptionPane.OK_OPTION) {
                processing.setInterrupted();
                Project.getCurrentProject().setStopThePresses(true);
                try {
                    processingThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.out);
                }
                doClose();
            }
        });

    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private void myInitComponents() {
        okButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
    }

    public void setProcessingFinished() {
        this.processingFinished = true;
    }

    /**
     * Callback for progress update
     *
     * @param fileName - file being processed
     */
    @Override
    public void setProcessingState(final String fileName) {
        EventQueue.invokeLater(() -> fileLabel.setText(fileName));
    }

    /**
     * Callback for progress update
     */
    public synchronized void setDone() {
        EventQueue.invokeLater(() -> {
            progressBar.setValue(100);
            processingFinished = true;
            okButton.setEnabled(true);
        });

    }

    /**
     * Callback for progress update
     *
     * @param total
     */
    @Override
    public void setTotalSize(long total) {
        this.total = total;
    }

    public void resetCurrentSize() {
        EventQueue.invokeLater(() -> progressBar.setValue(0));
    }

    /**
     * Callback for progress update
     *
     * @param fileName
     */
    public void updateProcessingFile(final String fileName) {
        final String displayName = (fileName.length() <= 72)
                ? fileName
                : "..." + fileName.substring(fileName.length() - 72);
        EventQueue.invokeLater(() -> fileLabel.setText(displayName));
    }

    /**
     * Callback for progress update
     *
     * @param size of files copied so far
     */
    public void updateProgress(long size) {
        final long value = total == 0 ? 0 : size * 100 / total;
        LOGGER.trace("Processing progress: {} of the total {}", value, total);
        EventQueue.invokeLater(() -> progressBar.setValue((int) value));
    }

    /**
     * Callback for progress update
     */
    public void startProcessing() {
        String projectName = Project.getCurrentProject().getProjectName();
        Stats.getInstance().setJobStarted(projectName);
        processingThread = new Thread(processing);
        processingThread.start();
    }

    public static void centerWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton okButton;
    private javax.swing.JProgressBar progressBar;
}
