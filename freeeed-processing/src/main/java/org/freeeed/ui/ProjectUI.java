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

import java.awt.*;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.*;

import org.freeeed.db.DbLocalUtils;

import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mark
 */
public class ProjectUI extends javax.swing.JDialog {

    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUI.class);
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - Mark Kerzner
    private JButton okButton;
    private JTabbedPane tabPanel;
    private JPanel inputsPanel;
    private JLabel projectCodeLabel;
    private JTextField projectCodeField;
    private JLabel projectNameLabel;
    private JTextField projectNameField;
    private JLabel projectInputsLabel;
    private JLabel networkHelpLabel;
    private JButton addFileButton;
    private JButton addNetworkButton;
    private JButton removeButton;
    private JScrollPane projectInputsScrollPanel;
    private JList<String> projectInputsList;
    private JPanel dataSourcePanel;
    private JRadioButton dataSourceButton1;
    private JRadioButton dataSourceButton2;
    private JComboBox<String> loadFormatChoice;
    private JButton assignCodeButton;
    private JPanel stagingPanel;
    private JLabel stagingZipSizeLabel;
    private JTextField stagingZipSizeText;
    private JCheckBox stageInPlaceCheck;
    private JButton explainButton;
    private JCheckBox sampleDataCheck;
    private JTextField percentText;
    private JLabel percentLabel;
    private JPanel metadataPanel;
    private JLabel fieldSeparatorLabel;
    private JComboBox<String> fieldSeparatorChoice;
    private JLabel labelMetadataCollected;
    private JRadioButton standardMetadataRadio;
    private JRadioButton allMetadataRadio;
    private JCheckBox denistCheck;
    private JCheckBox textInMetadataBox;
    private JPanel ocrPanel;
    private JCheckBox ocrCheck;
    private JPanel cullingPanel;
    private JLabel cullingLabel;
    private JLabel helpLabel;
    private JScrollPane cullingScrollPanel;
    private JTextArea cullingText;
    private JPanel imagingPanel;
    private JPanel jPanel2;
    private JCheckBox createPdfImageCheckBox;
    private JLabel jLabel2;
    private JCheckBox previewCheck;
    private JPanel searchPanel;
    private JPanel jPanel5;
    private JRadioButton luceneIndexEnabledRadioButton;
    private JRadioButton solrIndexEnabledRadioButton;
    private JRadioButton noIndexCreationRadioButton;
    private JButton cancelButton;
    // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;

    /**
     * Creates new form ProcessingParametersUI
     *
     * @param parent
     * @param modal
     */
    public ProjectUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
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

    private void assignCodeButtonActionPerformed(ActionEvent e) {
        assignCode();
    }

    private void sampleDataCheckActionPerformed(ActionEvent e) {
        if (sampleDataCheck.isSelected()) {
            percentText.setEnabled(true);
        } else {
            percentText.setEnabled(false);
        }
    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    // Generated using JFormDesigner Evaluation license - Mark Kerzner
    private void initComponents() {
        okButton = new JButton();
        tabPanel = new JTabbedPane();
        inputsPanel = new JPanel();
        projectCodeLabel = new JLabel();
        projectCodeField = new JTextField();
        projectNameLabel = new JLabel();
        projectNameField = new JTextField();
        projectInputsLabel = new JLabel();
        networkHelpLabel = new JLabel();
        addFileButton = new JButton();
        addNetworkButton = new JButton();
        removeButton = new JButton();
        projectInputsScrollPanel = new JScrollPane();
        projectInputsList = new JList<>();
        dataSourcePanel = new JPanel();
        dataSourceButton1 = new JRadioButton();
        dataSourceButton2 = new JRadioButton();
        loadFormatChoice = new JComboBox<>();
        assignCodeButton = new JButton();
        stagingPanel = new JPanel();
        stagingZipSizeLabel = new JLabel();
        stagingZipSizeText = new JTextField();
        stageInPlaceCheck = new JCheckBox();
        explainButton = new JButton();
        sampleDataCheck = new JCheckBox();
        percentText = new JTextField();
        percentLabel = new JLabel();
        metadataPanel = new JPanel();
        fieldSeparatorLabel = new JLabel();
        fieldSeparatorChoice = new JComboBox<>();
        labelMetadataCollected = new JLabel();
        standardMetadataRadio = new JRadioButton();
        allMetadataRadio = new JRadioButton();
        denistCheck = new JCheckBox();
        textInMetadataBox = new JCheckBox();
        ocrPanel = new JPanel();
        ocrCheck = new JCheckBox();
        cullingPanel = new JPanel();
        cullingLabel = new JLabel();
        helpLabel = new JLabel();
        cullingScrollPanel = new JScrollPane();
        cullingText = new JTextArea();
        imagingPanel = new JPanel();
        jPanel2 = new JPanel();
        createPdfImageCheckBox = new JCheckBox();
        jLabel2 = new JLabel();
        previewCheck = new JCheckBox();
        searchPanel = new JPanel();
        jPanel5 = new JPanel();
        luceneIndexEnabledRadioButton = new JRadioButton();
        solrIndexEnabledRadioButton = new JRadioButton();
        noIndexCreationRadioButton = new JRadioButton();
        cancelButton = new JButton();

        //======== this ========
        setTitle("Project Options");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeDialog(e);
            }
        });
        Container contentPane = getContentPane();

        //---- okButton ----
        okButton.setText("OK");
        okButton.addActionListener(e -> okButtonActionPerformed(e));

        //======== tabPanel ========
        {

            //======== inputsPanel ========
            {

                // JFormDesigner evaluation mark
                inputsPanel.setBorder(new javax.swing.border.CompoundBorder(
                    new javax.swing.border.TitledBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 0),
                        "JFormDesigner Evaluation", javax.swing.border.TitledBorder.CENTER,
                        javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dialog", java.awt.Font.BOLD, 12),
                        java.awt.Color.red), inputsPanel.getBorder())); inputsPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener(){public void propertyChange(java.beans.PropertyChangeEvent e){if("border".equals(e.getPropertyName()))throw new RuntimeException();}});


                //---- projectCodeLabel ----
                projectCodeLabel.setText("Project code");

                //---- projectNameLabel ----
                projectNameLabel.setText("Name");

                //---- projectInputsLabel ----
                projectInputsLabel.setText("Add/Remove input data:");

                //---- networkHelpLabel ----
                networkHelpLabel.setForeground(Color.blue);
                networkHelpLabel.setText("Help");
                networkHelpLabel.setToolTipText("Click here for help on URI");
                networkHelpLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        networkHelpLabelMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        networkHelpLabelMouseExited(e);
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        networkHelpLabelMousePressed(e);
                    }
                });

                //---- addFileButton ----
                addFileButton.setText("Add local folder or file");
                addFileButton.addActionListener(e -> addFileButtonActionPerformed(e));

                //---- addNetworkButton ----
                addNetworkButton.setText("Add network (URI) location");
                addNetworkButton.setToolTipText("<html>Add network location in the URI format. <br />\nExample of ftp access: <br />\nftp://user:password@ftp.example.com/path/file.zip\n</html>");
                addNetworkButton.addActionListener(e -> addNetworkButtonActionPerformed(e));

                //---- removeButton ----
                removeButton.setText("Remove");
                removeButton.setToolTipText("Remove local folder or network location from project inputs - \nthe data itself remains intact");
                removeButton.addActionListener(e -> removeButtonActionPerformed(e));

                //======== projectInputsScrollPanel ========
                {

                    //---- projectInputsList ----
                    projectInputsList.setModel(new AbstractListModel<String>() {
                        String[] values = {

                        };
                        @Override
                        public int getSize() { return values.length; }
                        @Override
                        public String getElementAt(int i) { return values[i]; }
                    });
                    projectInputsScrollPanel.setViewportView(projectInputsList);
                }

                //======== dataSourcePanel ========
                {
                    dataSourcePanel.setBorder(new TitledBorder("Data source"));

                    //---- dataSourceButton1 ----
                    dataSourceButton1.setText("eDiscovery");
                    dataSourceButton1.setToolTipText("<html>\nInput comes from \n<ul>\n<li> mail boxes</li>\n<li>loose files</li>\n<li>and any of the 1,400 files recognized by FreeEed for eDiscovery</li>\n</ul>\n</html>");
                    dataSourceButton1.addActionListener(e -> dataSourceButton1ActionPerformed(e));

                    //---- dataSourceButton2 ----
                    dataSourceButton2.setText("Load file");
                    dataSourceButton2.setToolTipText("<html>\nInput comes from a CSV file<br/>\n<ul>\n<li>It can be the result of eDiscovery</li>\n<li>Or any other metadata file</li>\n<li>Fields should be the same as output by FreeEed</li>\n</ul>\n</html>");
                    dataSourceButton2.addActionListener(e -> dataSourceButton2ActionPerformed(e));

                    //---- loadFormatChoice ----
                    loadFormatChoice.setModel(new DefaultComboBoxModel<>(new String[] {
                        "CSV",
                        "JSON"
                    }));

                    GroupLayout dataSourcePanelLayout = new GroupLayout(dataSourcePanel);
                    dataSourcePanel.setLayout(dataSourcePanelLayout);
                    dataSourcePanelLayout.setHorizontalGroup(
                        dataSourcePanelLayout.createParallelGroup()
                            .addGroup(dataSourcePanelLayout.createSequentialGroup()
                                .addComponent(dataSourceButton1)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dataSourceButton2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(loadFormatChoice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                    );
                    dataSourcePanelLayout.setVerticalGroup(
                        dataSourcePanelLayout.createParallelGroup()
                            .addGroup(dataSourcePanelLayout.createSequentialGroup()
                                .addGroup(dataSourcePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(dataSourceButton1)
                                    .addComponent(dataSourceButton2)
                                    .addComponent(loadFormatChoice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 10, Short.MAX_VALUE))
                    );
                }

                //---- assignCodeButton ----
                assignCodeButton.setText("Force-assign");
                assignCodeButton.addActionListener(e -> assignCodeButtonActionPerformed(e));

                GroupLayout inputsPanelLayout = new GroupLayout(inputsPanel);
                inputsPanel.setLayout(inputsPanelLayout);
                inputsPanelLayout.setHorizontalGroup(
                    inputsPanelLayout.createParallelGroup()
                        .addGroup(inputsPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(inputsPanelLayout.createParallelGroup()
                                .addGroup(inputsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addGroup(inputsPanelLayout.createSequentialGroup()
                                        .addGroup(inputsPanelLayout.createParallelGroup()
                                            .addComponent(projectCodeLabel)
                                            .addComponent(projectNameLabel))
                                        .addGap(27, 27, 27)
                                        .addGroup(inputsPanelLayout.createParallelGroup()
                                            .addGroup(inputsPanelLayout.createSequentialGroup()
                                                .addComponent(projectCodeField, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE)
                                                .addGap(18, 18, 18)
                                                .addComponent(assignCodeButton))
                                            .addComponent(projectNameField, GroupLayout.PREFERRED_SIZE, 603, GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(inputsPanelLayout.createSequentialGroup()
                                        .addComponent(projectInputsLabel)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(addFileButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(addNetworkButton)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(networkHelpLabel)
                                        .addGap(30, 30, 30)
                                        .addComponent(removeButton)))
                                .addComponent(projectInputsScrollPanel, GroupLayout.PREFERRED_SIZE, 722, GroupLayout.PREFERRED_SIZE)
                                .addComponent(dataSourcePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(77, Short.MAX_VALUE))
                );
                inputsPanelLayout.setVerticalGroup(
                    inputsPanelLayout.createParallelGroup()
                        .addGroup(inputsPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(inputsPanelLayout.createParallelGroup()
                                .addGroup(inputsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(projectCodeLabel)
                                    .addComponent(projectCodeField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addComponent(assignCodeButton, GroupLayout.Alignment.TRAILING))
                            .addGap(18, 18, 18)
                            .addGroup(inputsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(projectNameLabel)
                                .addComponent(projectNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(inputsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(projectInputsLabel)
                                .addComponent(removeButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addNetworkButton)
                                .addComponent(addFileButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(networkHelpLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGap(18, 18, 18)
                            .addComponent(projectInputsScrollPanel, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                            .addGap(27, 27, 27)
                            .addComponent(dataSourcePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addGap(25, 25, 25))
                );
            }
            tabPanel.addTab("Inputs", inputsPanel);

            //======== stagingPanel ========
            {

                //---- stagingZipSizeLabel ----
                stagingZipSizeLabel.setText("Staging zip size, GB");

                //---- stageInPlaceCheck ----
                stageInPlaceCheck.setText("Read files directly");
                stageInPlaceCheck.setToolTipText("");

                //---- explainButton ----
                explainButton.setText("?");
                explainButton.addActionListener(e -> explainButtonActionPerformed(e));

                //---- sampleDataCheck ----
                sampleDataCheck.setText("Sample data");
                sampleDataCheck.setToolTipText("<html>\nCareful please!<br/>\nThis option is to test processing. <br/>\nIt will only stage some files, not all, for processing. <br/>\nUse it at your own risk.\n</html>");
                sampleDataCheck.addActionListener(e -> sampleDataCheckActionPerformed(e));

                //---- percentText ----
                percentText.setToolTipText("<html>\n1 means 1% of the data<br/>\n.1 means 0.1% of the data<br/>\n50 means 50% of the data<br/>\nThe data for sampling is chosen randomly<br/>\n</html>");

                //---- percentLabel ----
                percentLabel.setText("%");

                GroupLayout stagingPanelLayout = new GroupLayout(stagingPanel);
                stagingPanel.setLayout(stagingPanelLayout);
                stagingPanelLayout.setHorizontalGroup(
                    stagingPanelLayout.createParallelGroup()
                        .addGroup(stagingPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(stagingPanelLayout.createParallelGroup()
                                .addGroup(stagingPanelLayout.createSequentialGroup()
                                    .addComponent(stagingZipSizeLabel)
                                    .addGap(106, 106, 106)
                                    .addComponent(stagingZipSizeText, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE))
                                .addGroup(stagingPanelLayout.createSequentialGroup()
                                    .addGroup(stagingPanelLayout.createParallelGroup()
                                        .addComponent(stageInPlaceCheck)
                                        .addComponent(sampleDataCheck))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(stagingPanelLayout.createParallelGroup()
                                        .addComponent(explainButton)
                                        .addGroup(stagingPanelLayout.createSequentialGroup()
                                            .addComponent(percentText, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(percentLabel)))))
                            .addContainerGap(454, Short.MAX_VALUE))
                );
                stagingPanelLayout.setVerticalGroup(
                    stagingPanelLayout.createParallelGroup()
                        .addGroup(stagingPanelLayout.createSequentialGroup()
                            .addGap(33, 33, 33)
                            .addGroup(stagingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(stagingZipSizeLabel)
                                .addComponent(stagingZipSizeText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(71, 71, 71)
                            .addGroup(stagingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(stageInPlaceCheck)
                                .addComponent(explainButton))
                            .addGap(40, 40, 40)
                            .addGroup(stagingPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(sampleDataCheck)
                                .addGroup(stagingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                    .addComponent(percentText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(percentLabel)))
                            .addContainerGap(199, Short.MAX_VALUE))
                );
            }
            tabPanel.addTab("Staging", stagingPanel);

            //======== metadataPanel ========
            {

                //---- fieldSeparatorLabel ----
                fieldSeparatorLabel.setText("Field separator");

                //---- fieldSeparatorChoice ----
                fieldSeparatorChoice.setModel(new DefaultComboBoxModel<>(new String[] {

                }));

                //---- labelMetadataCollected ----
                labelMetadataCollected.setText("Metadata collected");

                //---- standardMetadataRadio ----
                standardMetadataRadio.setSelected(true);
                standardMetadataRadio.setText("Standard");

                //---- allMetadataRadio ----
                allMetadataRadio.setText("All");

                //---- denistCheck ----
                denistCheck.setSelected(true);
                denistCheck.setText("Remove system files");

                //---- textInMetadataBox ----
                textInMetadataBox.setText("Insert text in metadata");
                textInMetadataBox.setToolTipText("Useful for Concordance and Hive load");

                GroupLayout metadataPanelLayout = new GroupLayout(metadataPanel);
                metadataPanel.setLayout(metadataPanelLayout);
                metadataPanelLayout.setHorizontalGroup(
                    metadataPanelLayout.createParallelGroup()
                        .addGroup(metadataPanelLayout.createSequentialGroup()
                            .addGap(28, 28, 28)
                            .addGroup(metadataPanelLayout.createParallelGroup()
                                .addComponent(textInMetadataBox)
                                .addComponent(denistCheck)
                                .addGroup(metadataPanelLayout.createSequentialGroup()
                                    .addComponent(labelMetadataCollected)
                                    .addGap(36, 36, 36)
                                    .addComponent(standardMetadataRadio)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(allMetadataRadio))
                                .addGroup(metadataPanelLayout.createSequentialGroup()
                                    .addComponent(fieldSeparatorLabel)
                                    .addGap(38, 38, 38)
                                    .addComponent(fieldSeparatorChoice, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(464, Short.MAX_VALUE))
                );
                metadataPanelLayout.setVerticalGroup(
                    metadataPanelLayout.createParallelGroup()
                        .addGroup(metadataPanelLayout.createSequentialGroup()
                            .addGap(46, 46, 46)
                            .addGroup(metadataPanelLayout.createParallelGroup()
                                .addComponent(fieldSeparatorLabel)
                                .addComponent(fieldSeparatorChoice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(25, 25, 25)
                            .addGroup(metadataPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(labelMetadataCollected)
                                .addComponent(standardMetadataRadio)
                                .addComponent(allMetadataRadio))
                            .addGap(30, 30, 30)
                            .addComponent(denistCheck)
                            .addGap(18, 18, 18)
                            .addComponent(textInMetadataBox)
                            .addContainerGap(206, Short.MAX_VALUE))
                );
            }
            tabPanel.addTab("Metadata", metadataPanel);

            //======== ocrPanel ========
            {

                //---- ocrCheck ----
                ocrCheck.setSelected(true);
                ocrCheck.setText("Perform OCR");

                GroupLayout ocrPanelLayout = new GroupLayout(ocrPanel);
                ocrPanel.setLayout(ocrPanelLayout);
                ocrPanelLayout.setHorizontalGroup(
                    ocrPanelLayout.createParallelGroup()
                        .addGroup(ocrPanelLayout.createSequentialGroup()
                            .addGap(15, 15, 15)
                            .addComponent(ocrCheck)
                            .addContainerGap(683, Short.MAX_VALUE))
                );
                ocrPanelLayout.setVerticalGroup(
                    ocrPanelLayout.createParallelGroup()
                        .addGroup(ocrPanelLayout.createSequentialGroup()
                            .addGap(31, 31, 31)
                            .addComponent(ocrCheck)
                            .addContainerGap(374, Short.MAX_VALUE))
                );
            }
            tabPanel.addTab("OCR", ocrPanel);

            //======== cullingPanel ========
            {

                //---- cullingLabel ----
                cullingLabel.setText("Culling expressions");

                //---- helpLabel ----
                helpLabel.setForeground(Color.blue);
                helpLabel.setText("Help");
                helpLabel.setToolTipText("<html>Each line is treated as a separate keyword or search expression<br>\nAll lines are considered as connected by a non-exclusive \"OR\"<br>\nField names are required, so for example you can have<br><br>\ncontent:email<br>\ncontent:data<br>\ntitle:meeting<br><br>\nFor the syntax of search expressions click on this \"Help\"</html>");
                helpLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        helpLabelMouseEntered(e);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        helpLabelMouseExited(e);
                    }
                    @Override
                    public void mousePressed(MouseEvent e) {
                        helpLabelMousePressed(e);
                    }
                });

                //======== cullingScrollPanel ========
                {

                    //---- cullingText ----
                    cullingText.setColumns(20);
                    cullingText.setRows(5);
                    cullingScrollPanel.setViewportView(cullingText);
                }

                GroupLayout cullingPanelLayout = new GroupLayout(cullingPanel);
                cullingPanel.setLayout(cullingPanelLayout);
                cullingPanelLayout.setHorizontalGroup(
                    cullingPanelLayout.createParallelGroup()
                        .addGroup(cullingPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(cullingPanelLayout.createParallelGroup()
                                .addComponent(cullingScrollPanel, GroupLayout.DEFAULT_SIZE, 804, Short.MAX_VALUE)
                                .addGroup(cullingPanelLayout.createSequentialGroup()
                                    .addComponent(cullingLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(helpLabel)
                                    .addGap(0, 0, Short.MAX_VALUE)))
                            .addContainerGap())
                );
                cullingPanelLayout.setVerticalGroup(
                    cullingPanelLayout.createParallelGroup()
                        .addGroup(cullingPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(cullingPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(cullingLabel)
                                .addComponent(helpLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cullingScrollPanel, GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                            .addContainerGap())
                );
            }
            tabPanel.addTab("Culling", cullingPanel);

            //======== imagingPanel ========
            {

                //======== jPanel2 ========
                {
                    jPanel2.setBorder(new TitledBorder("Imaging Properties"));

                    //---- createPdfImageCheckBox ----
                    createPdfImageCheckBox.setText("Create PDF Images, multi-page, for every file");

                    //---- jLabel2 ----
                    jLabel2.setText("Control PDF image creation by changing the properties below");

                    GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
                    jPanel2.setLayout(jPanel2Layout);
                    jPanel2Layout.setHorizontalGroup(
                        jPanel2Layout.createParallelGroup()
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup()
                                    .addComponent(jLabel2)
                                    .addComponent(createPdfImageCheckBox))
                                .addContainerGap(237, Short.MAX_VALUE))
                    );
                    jPanel2Layout.setVerticalGroup(
                        jPanel2Layout.createParallelGroup()
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(createPdfImageCheckBox)
                                .addContainerGap(61, Short.MAX_VALUE))
                    );
                }

                //---- previewCheck ----
                previewCheck.setText("Generate HTML documens for quick preview");
                previewCheck.setToolTipText("This option is for FreeEed Review. It generates HTML files for quick view");

                GroupLayout imagingPanelLayout = new GroupLayout(imagingPanel);
                imagingPanel.setLayout(imagingPanelLayout);
                imagingPanelLayout.setHorizontalGroup(
                    imagingPanelLayout.createParallelGroup()
                        .addGroup(imagingPanelLayout.createSequentialGroup()
                            .addGap(21, 21, 21)
                            .addGroup(imagingPanelLayout.createParallelGroup()
                                .addGroup(imagingPanelLayout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addComponent(previewCheck))
                                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addContainerGap(122, Short.MAX_VALUE))
                );
                imagingPanelLayout.setVerticalGroup(
                    imagingPanelLayout.createParallelGroup()
                        .addGroup(imagingPanelLayout.createSequentialGroup()
                            .addGap(19, 19, 19)
                            .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(previewCheck)
                            .addContainerGap(222, Short.MAX_VALUE))
                );
            }
            tabPanel.addTab("Imaging", imagingPanel);

            //======== searchPanel ========
            {

                //======== jPanel5 ========
                {
                    jPanel5.setBorder(new TitledBorder("Index options"));

                    //---- luceneIndexEnabledRadioButton ----
                    luceneIndexEnabledRadioButton.setText("Create Lucene index (for geeks)");

                    //---- solrIndexEnabledRadioButton ----
                    solrIndexEnabledRadioButton.setText("Prepare Solr search");

                    //---- noIndexCreationRadioButton ----
                    noIndexCreationRadioButton.setSelected(true);
                    noIndexCreationRadioButton.setText("No Search");

                    GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
                    jPanel5.setLayout(jPanel5Layout);
                    jPanel5Layout.setHorizontalGroup(
                        jPanel5Layout.createParallelGroup()
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel5Layout.createParallelGroup()
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(luceneIndexEnabledRadioButton)
                                        .addGap(0, 373, Short.MAX_VALUE))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup()
                                            .addComponent(noIndexCreationRadioButton)
                                            .addComponent(solrIndexEnabledRadioButton))
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    );
                    jPanel5Layout.setVerticalGroup(
                        jPanel5Layout.createParallelGroup()
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addComponent(noIndexCreationRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(solrIndexEnabledRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(luceneIndexEnabledRadioButton)
                                .addContainerGap(19, Short.MAX_VALUE))
                    );
                }

                GroupLayout searchPanelLayout = new GroupLayout(searchPanel);
                searchPanel.setLayout(searchPanelLayout);
                searchPanelLayout.setHorizontalGroup(
                    searchPanelLayout.createParallelGroup()
                        .addGroup(searchPanelLayout.createSequentialGroup()
                            .addGap(18, 18, 18)
                            .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(162, Short.MAX_VALUE))
                );
                searchPanelLayout.setVerticalGroup(
                    searchPanelLayout.createParallelGroup()
                        .addGroup(searchPanelLayout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addContainerGap(243, Short.MAX_VALUE))
                );
            }
            tabPanel.addTab("Search", searchPanel);
        }

        //---- cancelButton ----
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(e -> cancelButtonActionPerformed(e));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(cancelButton)
                    .addGap(14, 14, 14))
                .addComponent(tabPanel)
        );
        contentPaneLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {cancelButton, okButton});
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                    .addComponent(tabPanel, GroupLayout.PREFERRED_SIZE, 455, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelButton)
                        .addComponent(okButton))
                    .addContainerGap())
        );
        pack();
        setLocationRelativeTo(getOwner());

        //---- dataSourceButtonGroup ----
        ButtonGroup dataSourceButtonGroup = new ButtonGroup();
        dataSourceButtonGroup.add(dataSourceButton1);
        dataSourceButtonGroup.add(dataSourceButton2);

        //---- metadataButtonGroup ----
        ButtonGroup metadataButtonGroup = new ButtonGroup();
        metadataButtonGroup.add(standardMetadataRadio);
        metadataButtonGroup.add(allMetadataRadio);

        //---- searchButtonGroup ----
        ButtonGroup searchButtonGroup = new ButtonGroup();
        searchButtonGroup.add(luceneIndexEnabledRadioButton);
        searchButtonGroup.add(solrIndexEnabledRadioButton);
        searchButtonGroup.add(noIndexCreationRadioButton);
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        try {
            if (saveData() == false) {
                return;
            }
            Project project = Project.getCurrentProject();
            DbLocalUtils.saveProject(project);
            FreeEedUI.getInstance().updateTitle(project.getProjectName());
        } catch (Exception e) {
            LOGGER.error("Error saving project", e);
            JOptionPane.showMessageDialog(this, "Error saving project");
        }
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void helpLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_helpLabelMouseEntered

    private void helpLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_helpLabelMouseExited

    private void helpLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpLabelMousePressed
        openLuceneSyntaxBrowser();
    }//GEN-LAST:event_helpLabelMousePressed

    private void networkHelpLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_networkHelpLabelMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_networkHelpLabelMouseEntered

    private void networkHelpLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_networkHelpLabelMouseExited
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_networkHelpLabelMouseExited

    private void networkHelpLabelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_networkHelpLabelMousePressed
        openUriSyntaxBrowser();
    }//GEN-LAST:event_networkHelpLabelMousePressed

    private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileButtonActionPerformed
        addFileInput();
    }//GEN-LAST:event_addFileButtonActionPerformed

    private void addNetworkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNetworkButtonActionPerformed
        addUriInput();
    }//GEN-LAST:event_addNetworkButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        removeInput();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void explainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_explainButtonActionPerformed
        JOptionPane.showMessageDialog(this,
                "Staging step is always required.\n"
                        + "However, this option will bypass preparing data in zip files.\n"
                        + "Instead, data will be read directly from the source directories or zip files.\n"
                        + "To keep in mind:\n"
                        + "Directories need to be accessible from every Mapper if running on the cluster\n"
                        + "This option may make staging faster but processing slower");
    }//GEN-LAST:event_explainButtonActionPerformed

    private void dataSourceButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataSourceButton2ActionPerformed
        loadFormatChoice.setEnabled(true);
    }//GEN-LAST:event_dataSourceButton2ActionPerformed

    private void dataSourceButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataSourceButton1ActionPerformed
        loadFormatChoice.setEnabled(false);
    }//GEN-LAST:event_dataSourceButton1ActionPerformed

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    private void openLuceneSyntaxBrowser() {
        String url = "http://lucene.apache.org/core/old_versioned_docs/versions/3_5_0/queryparsersyntax.html";
        UtilUI.openBrowser(null, url);
    }

    private void openUriSyntaxBrowser() {
        String url = "http://en.wikipedia.org/wiki/URI_scheme#Generic_syntax";
        UtilUI.openBrowser(null, url);
    }

    private void showData() {
        showProjectInputs();
        showProcessingParametersData();
    }

    @SuppressWarnings("unchecked")
    private void showProjectInputs() {
        Project project = Project.getCurrentProject();
        setTitle("Settings for project " + project.getProjectName());
        projectCodeField.setText(project.getProjectCode());
        projectNameField.setText(project.getProjectName());

        DefaultListModel model = new DefaultListModel();
        String[] dirs = project.getInputs();
        String[] custodians = project.getCustodians(dirs);
        if (dirs != null) {
            for (int i = 0; i < dirs.length; ++i) {
                String custodian = i < custodians.length ? custodians[i] : "";
                String line = custodian + ": " + dirs[i];
                model.addElement(line.trim());
            }
        }
        int projectInputs = (dirs != null ? dirs.length : 0);
        projectInputsLabel.setText("Project inputs (" + projectInputs + ")");
        projectInputsList.setModel(model);
        cullingText.setText(project.getCullingAsTextBlock());
        // TODO the two lines below are side effect of UI
        String envSetting = Settings.getSettings().getEnv();
        project.setEnvironment(envSetting);
        double samplePercent = project.getSamplePercent();
        if (samplePercent > 0) {
            sampleDataCheck.setSelected(true);
            percentText.setEnabled(true);
            percentText.setText(Double.toString(samplePercent));
        } else {
            sampleDataCheck.setSelected(false);
            percentText.setEnabled(false);
            percentText.setText("");
        }
    }

    private boolean saveData() {
        boolean result = collectProjectInputs();
        if (result == false) {
            return false;
        }
        result = collectProcessingParametersData();
        if (result == false) {
            return false;
        }
        return true;
    }

    private boolean collectProjectInputs() {
        Project project = Project.getCurrentProject();
        project.setProjectName(projectNameField.getText());
        ListModel model = projectInputsList.getModel();
        String[] dirs = new String[model.getSize()];
        String[] custodians = new String[model.getSize()];
        for (int i = 0; i < model.getSize(); ++i) {
            String line = (String) model.getElementAt(i);
            int twodots = line.indexOf(":");
            String custodian = line.substring(0, twodots);
            String uri = line.substring(twodots + 2);
            custodians[i] = custodian.trim();
            dirs[i] = uri.trim();
        }
        project.setInputs(dirs);
        project.setCustodians(custodians);
        project.setEnvironment("local");
        project.setCulling(cullingText.getText());
        double samplePercent = 0;
        try {
            samplePercent = Double.parseDouble(percentText.getText());
            if (samplePercent < 0) samplePercent = 0;
            if (samplePercent >= 100) samplePercent = 0;
        } catch (Exception e) {
            samplePercent = 0;
        }
        project.setSamplePercent(samplePercent);
        return true;
    }

    private void removeInput() {
        int index = projectInputsList.getSelectedIndex();
        if (index >= 0) {
            ((DefaultListModel) projectInputsList.getModel()).remove(index);
        }
        projectInputsLabel.setText("Project inputs ("
                + projectInputsList.getModel().getSize() + ")");
    }

    @SuppressWarnings("unchecked")
    private void addFileInput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        File f = null;
        Settings settings = Settings.getSettings();
        if (settings.getCurrentDir() != null) {
            f = new File(settings.getCurrentDir());
        } else {
            try {
                f = new File(new File(".").getCanonicalPath());
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
        chooser.setCurrentDirectory(f);
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        if (file == null) {
            return;
        }
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File does not exist:\n" + file.getPath());
            return;
        }
        // Is this a directory with zip files only?
        boolean allZips = false;
        if (file.isDirectory()) {
            if (file.listFiles().length > 0) {
                allZips = true;
                for (File inside : file.listFiles()) {
                    if (!inside.isFile() || !inside.getName().toLowerCase().endsWith(".zip")) {
                        allZips = false;
                        break;
                    }
                }
            }
        }
        if (allZips) {
            int yesNo = JOptionPane.showConfirmDialog(this, "All files in this directory are zip file\n"
                    + "Package them separately as belonging to different custodians?\n"
                    + "(This will generate custodian names to be the same as file name)");
            if (yesNo != JOptionPane.YES_OPTION) {
                allZips = false;
            }
        }
        if (allZips) {
            File[] fileList = file.listFiles();
            Arrays.sort(fileList);
            for (File inside : fileList) {
                String custodian = "";
                String fileName = inside.getName();
                int lastUnderscore = fileName.lastIndexOf("_");
                if (lastUnderscore >= 0) {
                    custodian = fileName.substring(lastUnderscore + 1, fileName.length() - 4);
                }
                ((DefaultListModel) projectInputsList.getModel()).
                        addElement(custodian + ": " + inside.getPath());
            }
        } else {
            String custodian = "";
            if (dataSourceButton1.isSelected()) {
                custodian = JOptionPane.showInputDialog("Please enter custodian");
                if (custodian == null) {
                    return;
                }
            }
            ((DefaultListModel) projectInputsList.getModel()).addElement(custodian + ": " + file.getPath());
            projectInputsLabel.setText("Project inputs ("
                    + projectInputsList.getModel().getSize() + ")");
        }
        settings.setCurrentDir(file.getPath());
    }

    @SuppressWarnings("unchecked")
    private void addUriInput() {
        String uri = JOptionPane.showInputDialog(this, "Enter input's network locations as URI");
        if (uri == null) {
            return;
        }
        // TODO verify URI?        
        String custodian = JOptionPane.showInputDialog("Please enter custodian");
        if (custodian == null) {
            return;
        }
        ((DefaultListModel) projectInputsList.getModel()).addElement(custodian + ": " + uri);
    }

    private void showProcessingParametersData() {
        Project project = Project.getCurrentProject();
        int index = 0;
        String fieldSeparator = project.getFieldSeparator();
        switch (fieldSeparator) {
            case "tab":
                index = 0;
                break;
            case "hex_one":
                index = 1;
                break;
            case "pipe":
                index = 2;
                break;
            case "carret":
                index = 3;
                break;
        }
        fieldSeparatorChoice.setSelectedIndex(index);
        allMetadataRadio.setSelected("all".equals(project.getMetadataCollect()));
        standardMetadataRadio.setSelected("standard".equals(project.getMetadataCollect()));
        denistCheck.setSelected(project.isRemoveSystemFiles());
        textInMetadataBox.setSelected(project.isTextInMetadata());
        stagingZipSizeText.setText(Double.toString(project.getGigsPerArchive()));
        ocrCheck.setSelected(project.isOcrEnabled());

        luceneIndexEnabledRadioButton.setSelected(project.isLuceneIndexEnabled());
        solrIndexEnabledRadioButton.setSelected(project.isSendIndexToSolrEnabled());
        if (!project.isLuceneIndexEnabled() && !project.isSendIndexToSolrEnabled()) {
            noIndexCreationRadioButton.setSelected(true);
        }

        createPdfImageCheckBox.setSelected(project.isCreatePDF());
        previewCheck.setSelected(project.isPreview());
        dataSourceButton1.setSelected(project.getDataSource() == Project.DATA_SOURCE_EDISCOVERY);
        dataSourceButton2.setSelected(project.getDataSource() == Project.DATA_SOURCE_LOAD_FILE);
        loadFormatChoice.setEnabled(dataSourceButton2.isSelected());
        stageInPlaceCheck.setSelected(project.isStageInPlace());
        loadFormatChoice.setSelectedItem(Project.getCurrentProject().getLoadFileFormat().toUpperCase());
    }

    private boolean collectProcessingParametersData() {
        Project project = Project.getCurrentProject();
        try {
            int index = fieldSeparatorChoice.getSelectedIndex();
            switch (index) {
                case 0:
                    project.setFieldSeparator("tab");
                    break;
                case 1:
                    project.setFieldSeparator("hex_one");
                    break;
                case 2:
                    project.setFieldSeparator("pipe");
                    break;
                case 3:
                    project.setFieldSeparator("carret");
                    break;
            }
            project.setMetadataCollect(
                    standardMetadataRadio.isSelected() ? "standard" : "all");
            project.setRemoveSystemFiles(denistCheck.isSelected());
            project.setTextInMetadata(textInMetadataBox.isSelected());
            project.setGigsPerArchive(Double.parseDouble(stagingZipSizeText.getText()));
            project.setOcrEnabled(ocrCheck.isSelected());
            project.setLuceneIndexEnabled(luceneIndexEnabledRadioButton.isSelected());
            project.setSendIndexToSolrEnabled(solrIndexEnabledRadioButton.isSelected());
            project.setCreatePDF(createPdfImageCheckBox.isSelected());
            project.setPreview(previewCheck.isSelected());
            project.setDataSource(dataSourceButton1.isSelected() ? Project.DATA_SOURCE_EDISCOVERY : Project.DATA_SOURCE_LOAD_FILE);
            project.setStageInPlace(stageInPlaceCheck.isSelected());
            project.setLoadFileFormat((String) loadFormatChoice.getSelectedItem());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            Project project = Project.getCurrentProject();
            if (project == null) {
                JOptionPane.showMessageDialog(rootPane, "Create or open a project first");
                return;
            }
            myInit();
            projectCodeField.setEnabled(false);
            showData();
        }
        super.setVisible(b);
    }

    private void myInit() {
        fieldSeparatorChoice.removeAllItems();
        fieldSeparatorChoice.addItem("tab (\\t)");
        fieldSeparatorChoice.addItem("non-ascii one (x0001)");
        fieldSeparatorChoice.addItem("pipe (|)");
        fieldSeparatorChoice.addItem("carret (^)");
    }

    private void assignCode() {
        Project currentProject = Project.getCurrentProject();
        String currentCode = currentProject.getProjectCode();

        String newCodeString = JOptionPane.showInputDialog("Please choose the new code", Project.getCurrentProject().getProjectCode());
        if (newCodeString == null) {
            return;
        }
        int newCode = 0;
        try {
            newCode = Integer.parseInt(newCodeString);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter an integer number");
            return;
        }
        System.out.println(newCode);
        try {
            Project isProject = DbLocalUtils.getProject(newCode);
            if (isProject.getProjectCode() != null) {
                JOptionPane.showMessageDialog(null, "This project code is aleady in use");
                return;
            }
            currentProject.setProjectCode("" + newCode);
            DbLocalUtils.saveProject(currentProject);
            DbLocalUtils.deleteProject(Integer.parseInt(currentCode));
        } catch (Exception e) {
            LOGGER.error("Problem assignment new code", e);
            return;
        }
        projectCodeField.setText(newCodeString);
    }
}
