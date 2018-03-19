/*
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class RunModeUI extends javax.swing.JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunModeUI.class);
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;

    private final Frame parent;
    /**
     * Creates new form RunModeUI
     *  @param parent
     *
     */
    public RunModeUI(Frame parent) {
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
        java.awt.GridBagConstraints gridBagConstraints;

        choiceGroup = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        radioLocal = new javax.swing.JRadioButton();
        radioAWS = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        localExplain = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        awsExplain = new javax.swing.JTextArea();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        okButton.setText("OK");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 26;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 18, 16, 32);
        getContentPane().add(okButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(16, 64, 16, 0);
        getContentPane().add(cancelButton, gridBagConstraints);

        choiceGroup.add(radioLocal);
        radioLocal.setText("Local");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 26, 0, 0);
        getContentPane().add(radioLocal, gridBagConstraints);

        choiceGroup.add(radioAWS);
        radioAWS.setText("Amazon cloud (AWS)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(34, 26, 0, 0);
        getContentPane().add(radioAWS, gridBagConstraints);

        localExplain.setEditable(false);
        localExplain.setColumns(20);
        localExplain.setRows(5);
        localExplain.setText("In AWS mode, data is collected locally,\nbut then uploaded to Amazon Cloud.\nProject definitions are stored in the\ncloud, and processing is done in the cloud.");
        jScrollPane1.setViewportView(localExplain);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 277;
        gridBagConstraints.ipady = 90;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 26, 0, 32);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        awsExplain.setEditable(false);
        awsExplain.setColumns(20);
        awsExplain.setRows(5);
        awsExplain.setText("In local mode, project definitions and data \nare stored on the local computer. \nProcessing is done on the local computer.\n");
        jScrollPane2.setViewportView(awsExplain);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 277;
        gridBagConstraints.ipady = 90;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(27, 26, 0, 32);
        getContentPane().add(jScrollPane2, gridBagConstraints);

        pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        try {
            saveData();
        } catch (Exception e) {
            LOGGER.error("Problem saving parameters", e);
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

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | javax.swing.UnsupportedLookAndFeelException | IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RunModeUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            RunModeUI dialog = new RunModeUI(new javax.swing.JFrame());
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    private javax.swing.JTextArea awsExplain;
    private javax.swing.JButton cancelButton;
    private javax.swing.ButtonGroup choiceGroup;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea localExplain;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton radioAWS;
    private javax.swing.JRadioButton radioLocal;
    private int returnStatus = RET_CANCEL;

    @Override
    public void setVisible(boolean b) {
        if (b) {
            setLocationRelativeTo(parent);
            showData();
        }
        super.setVisible(b);
    }

    private void showData() {
        Mode mode = Mode.getInstance();
        radioAWS.setSelected(mode.getRunMode() == Mode.RUN_MODE.AWS);
        radioLocal.setSelected(mode.getRunMode() == Mode.RUN_MODE.LOCAL);
    }

    private void saveData() throws Exception {
        Mode mode = Mode.getInstance();
        if (radioAWS.isSelected()) {
            mode.setRunMode(Mode.RUN_MODE.AWS);
        } else if (radioLocal.isSelected()) {
            mode.setRunMode(Mode.RUN_MODE.LOCAL);
        }
        DbLocalUtils.saveMode();
    }
}
