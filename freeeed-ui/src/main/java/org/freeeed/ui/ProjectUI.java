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

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.freeeed.db.DbLocalUtils;
import org.freeeed.main.Language_English;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author mark
 */
public class ProjectUI extends JDialog {
    //TODO: Re-implement culling
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectUI.class);
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    private JRadioButton blockChainRadioButton;
    private JTextField fromTextField;
    private JTextField toTextField;

    private boolean isNewCase = true;
    int buttonWidth = 150, buttonHeight = 25;

    /**
     * Creates new form ProcessingParametersUI
     *
     * @param parent
     */
    public ProjectUI(Frame parent, boolean isNewCase) {
        super(parent, true);
        setLayout(null);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setBounds(new Rectangle(700, 590));
        setResizable(false);
        setLocationRelativeTo(parent);
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
        initComponents();
        this.isNewCase = isNewCase;

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        GridBagConstraints gridBagConstraints;
        ButtonGroup searchButtonGroup = new ButtonGroup();
        ButtonGroup metadataButtonGroup = new ButtonGroup();
        ButtonGroup dataSourceButtonGroup = new ButtonGroup();
        JButton okButton = new JButton();
        JTabbedPane tabPanel = new JTabbedPane();
        tabPanel.setPreferredSize(new Dimension(1000, 700));
        JPanel inputsPanel = new JPanel();


        projectInputsLabel = new JLabel();
        JLabel networkHelpLabel = new JLabel();
        // Variables declaration - do not modify

        JButton addNetworkButton = new JButton();

        JScrollPane projectInputsScrollPanel = new JScrollPane();
        projectInputsList = new JList();
        dataSourceButton1 = new JRadioButton();
        dataSourceButton2 = new JRadioButton();
        dataSourceButton3 = new JRadioButton();
        dataSourceButton4 = new JRadioButton();
        loadFormatChoice = new JComboBox<>();
        JPanel stagingPanel = new JPanel();
        JPanel metadataPanel = new JPanel();
        fieldSeparatorChoice = new JComboBox();
        standardMetadataRadio = new JRadioButton();
        allMetadataRadio = new JRadioButton();
        denistCheck = new JCheckBox();
        textInMetadataBox = new JCheckBox();
        JPanel ocrPanel = new JPanel();
        ocrCheck = new JCheckBox();
        JPanel cullingPanel = new JPanel();
        JLabel cullingLabel = new JLabel();
        JLabel helpLabel = new JLabel();
        JScrollPane cullingScrollPanel = new JScrollPane();
        cullingText = new JTextArea();
        JPanel imagingPanel = new JPanel();
        JPanel jPanel2 = new JPanel();
        createPdfImageCheckBox = new JCheckBox();
        JLabel jLabel2 = new JLabel();
        previewCheck = new JCheckBox();
        JPanel searchPanel = new JPanel();
        JPanel jPanel5 = new JPanel();


        Project currentProject = Project.getCurrentProject();
        JSpinner fromBlock = new JSpinner(new SpinnerNumberModel(currentProject.getBlockFrom(), 0, Integer.MAX_VALUE, 1));
        JSpinner toBlock = new JSpinner(new SpinnerNumberModel(currentProject.getBlockTo(), 0, Integer.MAX_VALUE, 1));
        ((JSpinner.DefaultEditor) fromBlock.getEditor()).getTextField().setColumns(6);
        ((JSpinner.DefaultEditor) toBlock.getEditor()).getTextField().setColumns(6);
        toBlock.addChangeListener(e -> {
            int value = (int) ((JSpinner) e.getSource()).getValue();
            currentProject.setBlockTo(value);
        });
        fromBlock.addChangeListener(e -> {
            int value = (int) ((JSpinner) e.getSource()).getValue();
            currentProject.setBlockFrom(value);
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeDialog(evt);
            }
        });

        setTitle("Project");

        okButton.setText(Language_English.OK);
        okButton.addActionListener(this::okButtonActionPerformed);
        okButton.setBounds(575, 520, 100, buttonHeight);
        getContentPane().add(okButton);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        cancelButton.setBounds(15, 520, 100, buttonHeight);
        getContentPane().add(cancelButton);


        JLabel projectNameLabel = new JLabel("Case Name");
        projectNameLabel.setBounds(15, 13, 90, 25);
        getContentPane().add(projectNameLabel);


        projectNameField = new JTextField();
        projectNameField.setBounds(100, 13, 400, 25);
        getContentPane().add(projectNameField);

        JLabel projectCodeLabel = new JLabel("Project ID");
        projectCodeLabel.setBounds(550, 13, 90, 25);
        getContentPane().add(projectCodeLabel);


        projectCodeField = new JTextField();
        projectCodeField.setBounds(625, 13, 50, 25);
        getContentPane().add(projectCodeField);

        projectInputsScrollPanel.setViewportView(projectInputsList);
        projectInputsScrollPanel.setBounds(15, 60, 660, 250);
        getContentPane().add(projectInputsScrollPanel);


        JButton addFileButton = new JButton("Add local folder or file");
        addFileButton.addActionListener(this::addFileButtonActionPerformed);
        addFileButton.setBounds(15, 320, 200, buttonHeight);
        getContentPane().add(addFileButton);


        JButton removeButton = new JButton("Remove");
        removeButton.setToolTipText("Remove local folder or network location from project inputs - \nthe data itself remains intact");
        removeButton.addActionListener(this::removeButtonActionPerformed);
        removeButton.setBounds(590, 320, 80, buttonHeight);
        getContentPane().add(removeButton);

        JPanel dataSourcePanel = new JPanel();


        dataSourcePanel.setBorder(BorderFactory.createTitledBorder(" Data source "));


        dataSourceButtonGroup.add(dataSourceButton1);
        dataSourceButton1.setText("eDiscovery");
        dataSourceButton1.setToolTipText("<html>\nInput comes from \n<ul>\n<li> mail boxes</li>\n<li>loose files</li>\n<li>and any of the 1,400 files recognized by FreeEed for eDiscovery</li>\n</ul>\n</html>");
        dataSourceButton1.addActionListener(this::dataSourceButton1ActionPerformed);
        dataSourcePanel.add(dataSourceButton1);


        dataSourceButtonGroup.add(dataSourceButton2);
        dataSourceButton2.setText("Load file");
        dataSourceButton2.setToolTipText("<html>\nInput comes from a load file<br/>\n<ul>\n<li>It can be a production result of an eDiscovery request</li>\n<li>Or any other metadata file\n</ul>\n</html>");
        dataSourceButton2.addActionListener(this::dataSourceButton2ActionPerformed);
        dataSourcePanel.add(dataSourceButton2);


        loadFormatChoice.setModel(new DefaultComboBoxModel<>(new String[]{"DAT", "CSV", "JSON"}));

        dataSourcePanel.add(loadFormatChoice);

        dataSourceButtonGroup.add(dataSourceButton3);
        dataSourceButton3.setText("Blockchain range : ");
        dataSourceButton3.setToolTipText("<html>\nInput comes from smart contracts in Ethereum Blockchain<br/>\n<ul>\n<li>You will have to set up Blockcain reading software on your computer</li>\n<li>And tell it what range of blocks to load\n</ul>\n</html>");
        dataSourceButton3.addActionListener(this::dataSourceButton3ActionPerformed);

        dataSourcePanel.add(dataSourceButton3);


        dataSourcePanel.add(fromBlock);

        dataSourcePanel.add(toBlock);

        dataSourceButtonGroup.add(dataSourceButton4);
        dataSourceButton4.setText("Quickbook files");
        dataSourceButton4.setToolTipText("<html>\nInput comes from a Quickbooks files<br/>\n<ul>\n<li>Quickbooks files are CSV</li>\n<li>but broken up according to QB convenstions\n</ul>\n</html>");
        dataSourceButton4.addActionListener(this::dataSourceButton4ActionPerformed);
        dataSourcePanel.add(dataSourceButton4);

        dataSourcePanel.setBounds(15, 350, 660, 85);
        getContentPane().add(dataSourcePanel);


        JPanel settingPanel = new JPanel();
        settingPanel.setBorder(BorderFactory.createTitledBorder(" Setting "));
        settingPanel.setBounds(15, 435, 660, 85);


        JLabel fieldSeparatorLabel = new JLabel("Field separator");
        settingPanel.add(fieldSeparatorLabel);
        settingPanel.add(fieldSeparatorChoice);


        JLabel labelMetadataCollected = new JLabel("Metadata collected");
        settingPanel.add(labelMetadataCollected);


        metadataButtonGroup.add(standardMetadataRadio);
        standardMetadataRadio.setText("Standard");
        settingPanel.add(standardMetadataRadio);

        metadataButtonGroup.add(allMetadataRadio);
        allMetadataRadio.setText("All");
        allMetadataRadio.setSelected(true);
        settingPanel.add(allMetadataRadio);


        ocrCheck.setSelected(true);
        ocrCheck.setText("Perform OCR");
        settingPanel.add(ocrCheck);


        createSearch = new JCheckBox("Create Search");
        settingPanel.add(createSearch);


        getContentPane().add(settingPanel);

    }

    private void okButtonActionPerformed(ActionEvent evt) {
        try {
            if (saveData() == false) {
                return;
            }
            Project project = Project.getCurrentProject();
            DbLocalUtils.saveProject(project);
        } catch (Exception e) {
            LOGGER.error("Error saving project", e);
            JOptionPane.showMessageDialog(this, "Error saving project");
        }
        doClose(RET_OK);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        doClose(RET_CANCEL);
    }

    /**
     * Closes the dialog
     */
    private void closeDialog(WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void helpLabelMouseEntered(MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void helpLabelMouseExited(MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void helpLabelMousePressed(MouseEvent evt) {
        openLuceneSyntaxBrowser();
    }

    private void networkHelpLabelMouseEntered(MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void networkHelpLabelMouseExited(MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void networkHelpLabelMousePressed(MouseEvent evt) {
        openUriSyntaxBrowser();
    }

    private void addFileButtonActionPerformed(ActionEvent evt) {
        addFileInput();
    }

    private void addNetworkButtonActionPerformed(ActionEvent evt) {
        addUriInput();
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        removeInput();
    }

    private void dataSourceButton1ActionPerformed(ActionEvent evt) {
        loadFormatChoice.setEnabled(false);
        Project.getCurrentProject().setDataSource(Project.DATA_SOURCE_EDISCOVERY);
    }

    private void dataSourceButton2ActionPerformed(ActionEvent evt) {
        loadFormatChoice.setEnabled(true);
        Project.getCurrentProject().setDataSource(Project.DATA_SOURCE_LOAD_FILE);
    }

    private void dataSourceButton3ActionPerformed(ActionEvent evt) {
        loadFormatChoice.setEnabled(false);
        Project.getCurrentProject().setDataSource(Project.DATA_SOURCE_BLOCKCHAIN);
    }

    private void dataSourceButton4ActionPerformed(ActionEvent evt) {
        Project.getCurrentProject().setDataSource(Project.DATA_SOURCE_QB);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        FreeEedUI.getInstance().initCaseList();
        dispose();
    }

    private JRadioButton allMetadataRadio;
    private JCheckBox createPdfImageCheckBox;
    private JTextArea cullingText;
    private JRadioButton dataSourceButton1;
    private JRadioButton dataSourceButton2;
    private JRadioButton dataSourceButton3;
    private JRadioButton dataSourceButton4;
    private JCheckBox denistCheck;
    private JComboBox fieldSeparatorChoice;
    private javax.swing.JComboBox<String> loadFormatChoice;
    private JCheckBox ocrCheck;
    private JCheckBox previewCheck;
    private JTextField projectCodeField;
    private JLabel projectInputsLabel;
    private JList projectInputsList;
    private JTextField projectNameField;
    private JRadioButton standardMetadataRadio;

    private JCheckBox createSearch;

    private JCheckBox textInMetadataBox;
    // End of variables declaration
    private int returnStatus = RET_CANCEL;

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
        String envSetting = Settings.getSettings().getEnv();
        project.setEnvironment(envSetting);
    }

    private boolean saveData() {
        boolean result = collectProjectInputs();
        if (result == false) {
            return false;
        }
        result = collectProcessingParametersData();
        return result != false;
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
        ocrCheck.setSelected(project.isOcrEnabled());
        createSearch.setSelected(project.isSendIndexToESEnabled());
        createPdfImageCheckBox.setSelected(project.isCreatePDF());
        previewCheck.setSelected(project.isPreview());
        dataSourceButton1.setSelected(project.getDataSource() == Project.DATA_SOURCE_EDISCOVERY);
        dataSourceButton2.setSelected(project.getDataSource() == Project.DATA_SOURCE_LOAD_FILE);
        dataSourceButton3.setSelected(project.getDataSource() == Project.DATA_SOURCE_BLOCKCHAIN);
        dataSourceButton4.setSelected(project.getDataSource() == Project.DATA_SOURCE_QB);
        loadFormatChoice.setEnabled(dataSourceButton2.isSelected());
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
            project.setMetadataCollect(standardMetadataRadio.isSelected() ? "standard" : "all");
            project.setRemoveSystemFiles(denistCheck.isSelected());
            project.setTextInMetadata(textInMetadataBox.isSelected());
            project.setOcrEnabled(ocrCheck.isSelected());
            project.setSendIndexToESEnabled(createSearch.isSelected());
            project.setCreatePDF(createPdfImageCheckBox.isSelected());
            project.setPreview(previewCheck.isSelected());
            project.setDataSource(getDataSourceSelected());
            project.setLoadFileFormat((String) loadFormatChoice.getSelectedItem());
            Object from = project.get(ParameterProcessing.FROM_BLOCK);
            if (from == null) {
                project.setBlockFrom(1);
            }
            Object to = project.get(ParameterProcessing.TO_BLOCK);
            if (to == null) {
                project.setBlockTo(10);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private int getDataSourceSelected() {
        if (dataSourceButton2.isSelected()) {
            return Project.DATA_SOURCE_LOAD_FILE;
        } else if (dataSourceButton3.isSelected()) {
            return Project.DATA_SOURCE_BLOCKCHAIN;
        } else if (dataSourceButton4.isSelected()) {
            return Project.DATA_SOURCE_QB;
        }
        return Project.DATA_SOURCE_EDISCOVERY;
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
}
