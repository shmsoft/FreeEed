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

import org.freeeed.main.FreeEedMain;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.Version;
import org.freeeed.services.*;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author mark
 */
public class FreeEedUI extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedUI.class);
    private static FreeEedUI instance;

    public static FreeEedUI getInstance() {
        return instance;
    }

    /**
     * Creates new form Main
     */
    public FreeEedUI() {

        LOGGER.info("Starting {}", Version.getVersionAndBuild());
        LOGGER.info("System check:");

        String systemCheckErrors = OsUtil.systemCheck();
        if (!systemCheckErrors.isEmpty()) {
            SystemCheckUI ui = new SystemCheckUI(this, true);
            ui.setSystemErrorsText(systemCheckErrors);
            ui.setVisible(true);
        }
        List<String> status = OsUtil.getSystemSummary();
        status.forEach(LOGGER::info);
        try {
            Mode.load();
            Settings.load();
        } catch (Exception e) {
            LOGGER.error("Problem initializing internal db");
        }
        initComponents();
        showHistory();
    }

    public void setInstance(FreeEedUI aInstance) {
        instance = aInstance;
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        mainMenu = new JMenuBar();
        fileMenu = new JMenu();
        menuItemProjects = new JMenuItem();
        menuItemExit = new JMenuItem();
        editMenu = new JMenu();
        menuItemProjectOptions = new JMenuItem();
        processMenu = new JMenu();
        stageMenuItem = new JMenuItem();
        processMenuItem = new JMenuItem();
        processSeparator = new Separator();
        ecProcessMenuItem = new JMenuItem();
        historyMenuItem = new JMenuItem();
        reviewMenu = new JMenu();
        menuItemOutputFolder = new JMenuItem();
        menuItemOpenSearchUI = new JMenuItem();
        menuItemOpenRawES = new JMenuItem();
        analyticsMenu = new JMenu();
        wordCloudMenuItem = new JMenuItem();
        settingsMenu = new JMenu();
        modeMenuItem = new JMenuItem();
        programSettingsMenuItem = new JMenuItem();
        s3SetupMenuItem = new JMenuItem();
        ec2SetupMenuItem = new JMenuItem();
        clusterMenuItem = new JMenuItem();
        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FreeEed - Graphical User Interface");
        fileMenu.setText("File");

        menuItemProjects.setText("Projects");
        menuItemProjects.addActionListener(this::menuItemProjectsActionPerformed);
        fileMenu.add(menuItemProjects);

        menuItemExit.setText("Exit");
        menuItemExit.addActionListener(this::menuItemExitActionPerformed);
        fileMenu.add(menuItemExit);

        mainMenu.add(fileMenu);

        editMenu.setText("Edit");

        menuItemProjectOptions.setText("Project options");
        menuItemProjectOptions.addActionListener(this::menuItemProjectOptionsActionPerformed);
        editMenu.add(menuItemProjectOptions);

        mainMenu.add(editMenu);

        processMenu.setText("Process");

        stageMenuItem.setText("Stage");
        stageMenuItem.addActionListener(this::stageMenuItemActionPerformed);
        processMenu.add(stageMenuItem);

        processMenuItem.setText("Process locally");
        processMenuItem.addActionListener(this::processMenuItemActionPerformed);
        processMenu.add(processMenuItem);
        processMenu.add(processSeparator);

        ecProcessMenuItem.setText("Process on Amazon");
        ecProcessMenuItem.addActionListener(this::ecProcessMenuItemActionPerformed);
        processMenu.add(ecProcessMenuItem);

        historyMenuItem.setText("History");
        historyMenuItem.addActionListener(this::historyMenuItemActionPerformed);
        processMenu.add(historyMenuItem);

        mainMenu.add(processMenu);

        reviewMenu.setText("Review");

        menuItemOutputFolder.setText("See output files");
        menuItemOutputFolder.addActionListener(this::menuItemOutputFolderActionPerformed);
        reviewMenu.add(menuItemOutputFolder);

        menuItemOpenSearchUI.setText("Go to review");
        menuItemOpenSearchUI.addActionListener(this::menuItemOpenSearchUIActionPerformed);
        reviewMenu.add(menuItemOpenSearchUI);

        menuItemOpenRawES.setText("Open ElasticSearch index");
        menuItemOpenRawES.addActionListener(this::menuItemOpenRawESActionPerformed);
        reviewMenu.add(menuItemOpenRawES);

        mainMenu.add(reviewMenu);

        analyticsMenu.setText("Analytics");

        wordCloudMenuItem.setText("Word Cloud");
        wordCloudMenuItem.addActionListener(this::wordCloudMenuItemActionPerformed);
        analyticsMenu.add(wordCloudMenuItem);

        mainMenu.add(analyticsMenu);

        settingsMenu.setText("Settings");

        modeMenuItem.setText("Run mode");
        modeMenuItem.addActionListener(this::modeMenuItemActionPerformed);
        settingsMenu.add(modeMenuItem);

        programSettingsMenuItem.setText("Program settings");
        programSettingsMenuItem.addActionListener(this::programSettingsMenuItemActionPerformed);
        settingsMenu.add(programSettingsMenuItem);

        s3SetupMenuItem.setText("S3 settings");
        s3SetupMenuItem.addActionListener(this::s3SetupMenuItemActionPerformed);
        settingsMenu.add(s3SetupMenuItem);

        ec2SetupMenuItem.setText("EC2 settings");
        ec2SetupMenuItem.addActionListener(this::ec2SetupMenuItemActionPerformed);
        settingsMenu.add(ec2SetupMenuItem);

        clusterMenuItem.setText("EC2 cluster control");
        clusterMenuItem.addActionListener(this::clusterMenuItemActionPerformed);
        settingsMenu.add(clusterMenuItem);

        mainMenu.add(settingsMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(this::aboutMenuItemActionPerformed);
        helpMenu.add(aboutMenuItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);
        mainMenu.setLayout(new GridBagLayout());
        pack();

    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        new AboutDialog(this).setVisible(true);
    }

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            exitApp();
        } catch (Exception e) {
            LOGGER.error("Error saving project", e);
            JOptionPane.showMessageDialog(this, "Application error " + e.getMessage());
        }
    }

    private void menuItemProjectsActionPerformed(java.awt.event.ActionEvent evt) {
        openProject();
    }

    private void stageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        stageProject();
    }

    private void processMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        processProject();
    }

    private void historyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        showHistory();
    }

    private void menuItemOutputFolderActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            openOutputFolder();
        } catch (IOException e) {
            LOGGER.error("Could not open folder", e);
            JOptionPane.showMessageDialog(this, "Somthing is wrong with the OS, please open the output folder manually");
        }
    }

    private void s3SetupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        S3SetupUI ui = new S3SetupUI(this);
        ui.setVisible(true);
    }

    private void ec2SetupMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        EC2SetupUI ui = new EC2SetupUI(this);
        ui.setVisible(true);
    }

    private void clusterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        ClusterControlUI ui = new ClusterControlUI(this);
        ui.setVisible(true);
    }

    private void ecProcessMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        if (Project.getCurrentProject().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create or open a project first");
            return;
        }
        EC2ProcessUI ui = new EC2ProcessUI(this);
        ui.setVisible(true);
    }

    private void menuItemProjectOptionsActionPerformed(java.awt.event.ActionEvent evt) {
        showProcessingOptions();
    }

    private void menuItemOpenSearchUIActionPerformed(java.awt.event.ActionEvent evt) {
        openReviewUI();
    }

    private void programSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        openProgramSettings();
    }

    private void menuItemOpenRawESActionPerformed(java.awt.event.ActionEvent evt) {
        openES();
    }

    private void modeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        openModeUI();
    }

    private void wordCloudMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        openWordCloudUI();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {
            FreeEedUI ui = new FreeEedUI();
            ui.setInstance(ui);
            Services.start();
            ui.setVisible(true);
        });
    }

    private JMenuItem aboutMenuItem;
    private JMenu analyticsMenu;
    private JMenuItem clusterMenuItem;
    private JMenuItem ec2SetupMenuItem;
    private JMenuItem ecProcessMenuItem;
    private JMenu editMenu;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenuItem historyMenuItem;
    private JMenuBar mainMenu;
    private JMenuItem menuItemExit;
    private JMenuItem menuItemOpenRawES;
    private JMenuItem menuItemOpenSearchUI;
    private JMenuItem menuItemOutputFolder;
    private JMenuItem menuItemProjectOptions;
    private JMenuItem menuItemProjects;
    private JMenuItem modeMenuItem;
    private JMenu processMenu;
    private JMenuItem processMenuItem;
    private Separator processSeparator;
    private JMenuItem programSettingsMenuItem;
    private JMenu reviewMenu;
    private JMenuItem s3SetupMenuItem;
    private JMenu settingsMenu;
    private JMenuItem stageMenuItem;
    private JMenuItem wordCloudMenuItem;

    @Override
    public void setVisible(boolean b) {
        if (b) {
            myInitComponents();
        }
        super.setVisible(b);
    }

    private void myInitComponents() {
        addWindowListener(new FrameListener());
        setBounds(64, 40, 800, 500);
        setLocationRelativeTo(null);
        setTitle(ParameterProcessing.APP_NAME + ParameterProcessing.TM + " - e-Discovery, Search and Analytics Platform");
    }

    private void exitApp() throws Exception {
        if (!isExitAllowed()) {
            return;
        }
        Settings.getSettings().save();
        setVisible(false);
        System.exit(0);
    }

    private boolean isExitAllowed() {
        return true;
    }

    private void openProject() {
        ProjectsUI dialog = new ProjectsUI(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void openProject(File selectedFile) {
        Project project = Project.loadFromFile(selectedFile);
        project.setProjectFilePath(selectedFile.getPath());
        updateTitle(project.getProjectCode() + " " + project.getProjectName());
        LOGGER.trace("Opened project file: " + selectedFile.getPath());
        Settings settings = Settings.getSettings();
        settings.addRecentProject(selectedFile.getPath());
        showProcessingOptions();
    }

    private class ProjectFilter extends FileFilter {

        @Override
        public boolean accept(File file) {
            String filename = file.getName();
            return filename.endsWith(".project") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Project files";
        }
    }

    public void updateTitle(String title) {
        if (title != null) {
            setTitle(ParameterProcessing.APP_NAME + ParameterProcessing.TM + " - " + title);
        } else {
            setTitle(ParameterProcessing.APP_NAME + ParameterProcessing.TM);
        }
    }

    public void showProcessingOptions() {
        if (Project.getCurrentProject().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create or open a project first");
            return;
        }
        ProjectUI dialog = new ProjectUI(this);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void stageProject() {
        Project project = Project.getCurrentProject();
        if (project.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create or open a project first");
            return;
        }
        // check for empty input directories
        String[] dirs = project.getInputs();
        if (dirs.length == 0) {
            JOptionPane.showMessageDialog(rootPane, "You selected no data to stage");
            return;
        }
        for (String dir : dirs) {
            File file = new File(dir);
            if (file.isDirectory() && file.list().length == 0) {
                JOptionPane.showMessageDialog(rootPane, "Some of the directories you are trying to stage are empty. "
                        + "\\It does not make sense to stage them and may lead to confusion."
                        + "\\Please check the project directories");
                return;
            }
        }
        try {
            FreeEedMain.getInstance().runStagePackageInput();
        } catch (Exception e) {
            LOGGER.error("Error staging project", e);
        }
    }

    private void runProcessing() throws IllegalStateException {
        Project project = Project.getCurrentProject();
        if (project.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create or open a project first");
            return;
        }
        project.setEnvironment(Project.ENV_LOCAL);
        FreeEedMain mainInstance = FreeEedMain.getInstance();
        if (new File(project.getResultsDir()).exists()) {
            // in most cases, it won't already exist, but just in case
            try {
                Util.deleteDirectory(new File(project.getResultsDir()));
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage());
            }
        }
        String processWhere = project.getProcessWhere();
        if (processWhere != null) {
            mainInstance.runProcessing(processWhere);
        } else {
            throw new IllegalStateException("No processing option selected.");
        }
    }

    private void showHistory() {
        HistoryUI ui = new HistoryUI();
        ui.setVisible(true);
    }

    private boolean areResultsPresent() {

        Project project = Project.getCurrentProject();
        if (project == null || project.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please open a project first");
            return false;
        }
        try {
            boolean success = Review.deliverFiles();
            if (!success) {
                JOptionPane.showMessageDialog(this, "No results yet");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("Problem while checking for results", e);
            return false;
        }
        return true;
    }

    private void openOutputFolder() throws IOException {
        if (!areResultsPresent()) {
            return;
        }
        String resultsFolder = Project.getCurrentProject().getResultsDir();
        try {
            // Desktop should work, but it stopped lately in Ubuntu
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(resultsFolder));
            } else if (OsUtil.isLinux()) {
                String command = "nautilus " + resultsFolder;
                OsUtil.runCommand(command);
            } else if (OsUtil.isMac()) {
                String command = "open " + resultsFolder;
                OsUtil.runCommand(command);
            }
        } catch (IOException ex) {
            LOGGER.error("error OS util ", ex);
        }
    }

    class FrameListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            try {
                Settings.getSettings().save();
            } catch (Exception ex) {
                LOGGER.error("Error saving project", ex);
                JOptionPane.showMessageDialog(null, "Application error " + ex.getMessage());
            }
        }
    }

    private void openWiki() {
        Settings settings = Settings.getSettings();
        String url = settings.getManualPage();
        UtilUI.openBrowser(this, url);
    }

    private void openProgramSettings() {
        ProgramSettingsUI programSettingsUI = new ProgramSettingsUI(this, true);
        programSettingsUI.setVisible(true);
    }

    private void openES() {
        Settings settings = Settings.getSettings();
        String url = settings.getESEndpoint();
        UtilUI.openBrowser(this, url);
    }

    private void openReviewUI() {
        Settings settings = Settings.getSettings();
        String url = settings.getReviewEndpoint();
        UtilUI.openBrowser(this, url);
    }

    public void processProject() {
        try {
            runProcessing();
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(this, "There were some problems with processing, \""
                    + e.getMessage() + "\n"
                    + "please check console output");
        }
    }

    private void openModeUI() {
        RunModeUI ui = new RunModeUI(this);
        ui.setVisible(true);
    }

    private void openWordCloudUI() {
        Project project = Project.getCurrentProject();
        if (project.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "Please open a project first");
            return;
        }
        WordCloudUI ui = new WordCloudUI(this, true);
        ui.setVisible(true);
    }
}
