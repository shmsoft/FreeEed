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
 * AboutDialog.java
 *
 * Created on Jun 7, 2011, 7:28:28 AM
 */
package org.freeeed.ui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.Version;

/**
 * @author mark
 */
public class AboutDialog extends JDialog {

    /**
     * A return status code - returned if Cancel button has been pressed
     */
    private static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    private static final int RET_OK = 1;

    private final Frame parent;

    /**
     * Creates new form AboutDialog
     *
     * @param parent - parent frame
     */
    public AboutDialog(final Frame parent) {
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

        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        JScrollPane aboutScrollPane = new JScrollPane();
        aboutText = new JTextArea();

        setTitle("About FreeEed (TM)");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        okButton.setText("OK");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 26;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 253, 16, 0);
        getContentPane().add(okButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 9, 16, 15);
        getContentPane().add(cancelButton, gridBagConstraints);

        aboutText.setColumns(20);
        aboutText.setRows(5);
        aboutScrollPane.setViewportView(aboutText);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 373;
        gridBagConstraints.ipady = 265;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(25, 15, 0, 15);
        getContentPane().add(aboutScrollPane, gridBagConstraints);

        pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
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

    private JTextArea aboutText;
    private int returnStatus = RET_CANCEL;

    private void myInitComponents() {
        String aboutTextStr =
                Version.getVersionAndBuild()
                        + "\n"
                        + "\n"
                        + "FreeEed" + ParameterProcessing.TM + " Player"
                        + "\n"
                        + "For additional information, please visit www.freeeed.org"
                        + "\n"
                        + "\n"
                        + "Brought to you by the FreeEed" + ParameterProcessing.TM + " team \n\n"
                        +
                        "Available CPU cores: " + Runtime.getRuntime().availableProcessors() + "\n"
                        +
                        "Available RAM: " + (float) (Runtime.getRuntime().totalMemory() / 1024) / 1024 + "Mb";

        aboutText.setText(aboutTextStr);
        aboutText.setEditable(false);
        setTitle("About " + ParameterProcessing.APP_NAME);
        setLocationRelativeTo(parent);
    }

    @Override
    public void setVisible(boolean b) {
        myInitComponents();
        super.setVisible(b);
    }
}
