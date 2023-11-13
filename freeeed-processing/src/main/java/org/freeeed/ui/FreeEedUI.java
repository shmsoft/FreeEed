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

import org.freeeed.api.tika.RestApiTika;
import org.freeeed.main.FreeEedMain;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.Version;
import org.freeeed.services.*;
import org.freeeed.util.LogFactory;
import org.freeeed.util.OsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author mark
 */
public class FreeEedUI extends javax.swing.JFrame {

    private final static Logger LOGGER = LogFactory.getLogger(FreeEedUI.class.getName());

    private static FreeEedUI instance;

    public static FreeEedUI getInstance() {
        return instance;
    }

    /**
     * Creates new form Main
     */
    public FreeEedUI() {
        LOGGER.info("Starting " + Version.getVersionAndBuild());
        LOGGER.info("System check:");
        if (OsUtil.isWindows()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                LOGGER.severe("UI ERROR " + e.getMessage());
            }
        }
        String systemCheckErrors = OsUtil.systemCheck();
        if (!systemCheckErrors.isEmpty()) {
            SystemCheckUI ui = new SystemCheckUI(this, true);
            ui.setSystemErrorsText(systemCheckErrors);
            ui.setVisible(true);
        }
        List<String> systemSummary = OsUtil.getSystemSummary();
        for (String stat : systemSummary) {
            LOGGER.info(stat);
        }
        List<String> serviceSummary = OsUtil.getServiceSummary();
        for (String stat : serviceSummary) {
            LOGGER.info(stat);
        }
        try {
            Mode.load();
            Settings.load();
        } catch (Exception e) {
            LOGGER.severe("Problem initializing internal db");
        }


        initComponents();
        // TODO???
        // startSolr();
    }

    public void setInstance(FreeEedUI aInstance) {
        instance = aInstance;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new java.awt.Panel();
        jSeparator1 = new javax.swing.JSeparator();
        mainMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuItemProjects = new javax.swing.JMenuItem();
        menuItemExit = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        menuItemProjectOptions = new javax.swing.JMenuItem();
        processMenu = new javax.swing.JMenu();
        stageMenuItem = new javax.swing.JMenuItem();
        processMenuItem = new javax.swing.JMenuItem();
        processSeparator = new javax.swing.JPopupMenu.Separator();
        historyMenuItem = new javax.swing.JMenuItem();
        reviewMenu = new javax.swing.JMenu();
        menuItemOutputFolder = new javax.swing.JMenuItem();
        menuItemOpenSearchUI = new javax.swing.JMenuItem();
        menuItemOpenRawSolr = new javax.swing.JMenuItem();
        analyticsMenu = new javax.swing.JMenu();
        wordCloudMenuItem = new javax.swing.JMenuItem();
        settingsMenu = new javax.swing.JMenu();
        programSettingsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        manualMenuItem = new javax.swing.JMenuItem();

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FreeEed - Graphical User Interface");
        setResizable(false);
        setSize(new java.awt.Dimension(670, 500));

        fileMenu.setText("File");

        menuItemProjects.setText("Projects");
        menuItemProjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemProjectsActionPerformed(evt);
            }
        });
        fileMenu.add(menuItemProjects);

        menuItemExit.setText("Exit");
        menuItemExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemExitActionPerformed(evt);
            }
        });
        fileMenu.add(menuItemExit);

        mainMenu.add(fileMenu);

        editMenu.setText("Edit");

        menuItemProjectOptions.setText("Project options");
        menuItemProjectOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemProjectOptionsActionPerformed(evt);
            }
        });
        editMenu.add(menuItemProjectOptions);

        mainMenu.add(editMenu);

        processMenu.setText("Process");

        stageMenuItem.setText("Stage");
        stageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stageMenuItemActionPerformed(evt);
            }
        });
        processMenu.add(stageMenuItem);

        processMenuItem.setText("Process");
        processMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                processMenuItemActionPerformed(evt);
            }
        });
        processMenu.add(processMenuItem);
        processMenu.add(processSeparator);

        historyMenuItem.setText("History");
        historyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historyMenuItemActionPerformed(evt);
            }
        });
        processMenu.add(historyMenuItem);

        mainMenu.add(processMenu);

        reviewMenu.setText("Review");

        menuItemOutputFolder.setText("See output files");
        menuItemOutputFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOutputFolderActionPerformed(evt);
            }
        });
        reviewMenu.add(menuItemOutputFolder);

        menuItemOpenSearchUI.setText("Go to review");
        menuItemOpenSearchUI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenSearchUIActionPerformed(evt);
            }
        });
        reviewMenu.add(menuItemOpenSearchUI);

        menuItemOpenRawSolr.setText("Open SOLR index");
        menuItemOpenRawSolr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemOpenRawSolrActionPerformed(evt);
            }
        });
        reviewMenu.add(menuItemOpenRawSolr);

        mainMenu.add(reviewMenu);

        analyticsMenu.setText("Analytics");

        wordCloudMenuItem.setText("Word Cloud");
        wordCloudMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wordCloudMenuItemActionPerformed(evt);
            }
        });
        analyticsMenu.add(wordCloudMenuItem);

        mainMenu.add(analyticsMenu);

        settingsMenu.setText("Settings");

        programSettingsMenuItem.setText("Program settings");
        programSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                programSettingsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(programSettingsMenuItem);

        mainMenu.add(settingsMenu);

        helpMenu.setText("Help");

        aboutMenuItem.setText("About");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        manualMenuItem.setText("Manual");
        manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(manualMenuItem);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 658, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(413, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void changelogMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        UtilUI.openBrowser(FreeEedUI.getInstance(), "https://github.com/shmsoft/FreeEed/wiki/Changelog");
    }

    private void manualMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        UtilUI.openBrowser(FreeEedUI.getInstance(), "https://github.com/shmsoft/FreeEed/wiki");
    }

    private void menuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemExitActionPerformed
        try {
            exitApp();
        } catch (Exception e) {
            LOGGER.severe("Error saving project");
            JOptionPane.showMessageDialog(this, "Application error " + e.getMessage());
        }
    }//GEN-LAST:event_menuItemExitActionPerformed

    private void menuItemProjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemProjectsActionPerformed
        openProject();
    }//GEN-LAST:event_menuItemProjectsActionPerformed

    private void stageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stageMenuItemActionPerformed
        stageProject();
    }//GEN-LAST:event_stageMenuItemActionPerformed

    private void processMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_processMenuItemActionPerformed
        processProject();
    }//GEN-LAST:event_processMenuItemActionPerformed

    private void historyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historyMenuItemActionPerformed
        showHistory();
    }//GEN-LAST:event_historyMenuItemActionPerformed

    private void menuItemOutputFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOutputFolderActionPerformed
        try {
            openOutputFolder();
        } catch (IOException e) {
            LOGGER.severe("Could not open folder");
            JOptionPane.showMessageDialog(this, "Something is wrong with the OS, please open the output folder manually");
        }
    }//GEN-LAST:event_menuItemOutputFolderActionPerformed

    private void menuItemProjectOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemProjectOptionsActionPerformed
        showProcessingOptions();
    }//GEN-LAST:event_menuItemProjectOptionsActionPerformed

    private void menuItemOpenSearchUIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenSearchUIActionPerformed
        openReviewUI();
    }//GEN-LAST:event_menuItemOpenSearchUIActionPerformed

    private void programSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_programSettingsMenuItemActionPerformed
        openProgramSettings();
    }//GEN-LAST:event_programSettingsMenuItemActionPerformed

    private void menuItemOpenRawSolrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemOpenRawSolrActionPerformed
        openSolr();
    }//GEN-LAST:event_menuItemOpenRawSolrActionPerformed

    private void wordCloudMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wordCloudMenuItemActionPerformed
        openWordCloudUI();
    }//GEN-LAST:event_wordCloudMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(() -> {
            FreeEedUI ui = new FreeEedUI();
            ui.setInstance(ui);
            ui.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenu analyticsMenu;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem historyMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenuItem manualMenuItem;
    private javax.swing.JMenuItem menuItemExit;
    private javax.swing.JMenuItem menuItemOpenRawSolr;
    private javax.swing.JMenuItem menuItemOpenSearchUI;
    private javax.swing.JMenuItem menuItemOutputFolder;
    private javax.swing.JMenuItem menuItemProjectOptions;
    private javax.swing.JMenuItem menuItemProjects;
    private java.awt.Panel panel1;
    private javax.swing.JMenu processMenu;
    private javax.swing.JMenuItem processMenuItem;
    private javax.swing.JPopupMenu.Separator processSeparator;
    private javax.swing.JMenuItem programSettingsMenuItem;
    private javax.swing.JMenu reviewMenu;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JMenuItem stageMenuItem;
    private javax.swing.JMenuItem wordCloudMenuItem;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setVisible(boolean b) {
        if (b) {
            myInitComponents();
        }
        super.setVisible(b);
    }

    private void myInitComponents() {
        addWindowListener(new FrameListener());
        setBounds(64, 40, 640, 400);
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
        ProjectsUI dialog = new ProjectsUI(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void openProject(File selectedFile) {
        Project project = Project.loadFromFile(selectedFile);
        project.setProjectFilePath(selectedFile.getPath());
        updateTitle(project.getProjectCode() + " " + project.getProjectName());
        LOGGER.finest("Opened project file: " + selectedFile.getPath());
        Settings settings = Settings.getSettings();
        settings.addRecentProject(selectedFile.getPath());
        showProcessingOptions();
    }

    private class ProjectFilter extends javax.swing.filechooser.FileFilter {

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
        ProjectUI dialog = new ProjectUI(this, true);
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
            LOGGER.severe("Error staging project");
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
        try {
            Project project = Project.getCurrentProject();
            if (project == null || project.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please open a project first");
                return false;
            }
            boolean success = Review.deliverFiles();
            if (!success) {
                JOptionPane.showMessageDialog(this, "No results yet");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warning("Results present? Problem!");
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
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(resultsFolder));
            } else if (OsUtil.isLinux()) {
                String command = "nautilus " + resultsFolder;
                OsUtil.runCommand(command);
            } else if (OsUtil.isMac()) {
                String command = "open " + resultsFolder;
                OsUtil.runCommand(command);
            }
        } catch (IOException e) {
            if (OsUtil.isLinux()) {
                String command = "nautilus " + resultsFolder;
                OsUtil.runCommand(command);
            } else if (OsUtil.isMac()) {
                String command = "open " + resultsFolder;
                OsUtil.runCommand(command);
            }
        }
    }

    class FrameListener extends WindowAdapter {

        @Override
        public void windowClosing(WindowEvent e) {
            try {
                Settings.getSettings().save();
            } catch (Exception ex) {
                LOGGER.severe("Error saving project");
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

    private void openSolr() {
        Settings settings = Settings.getSettings();
        String url = settings.getSolrEndpoint() + "/solr/admin";
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
        RunModeUI ui = new RunModeUI(this, true);
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

    private void startSolr() {
        String command = "cd freeeed-solr/example; java -Xmx1024M -jar start.jar &";
        try {
            OsUtil.runCommand(command);
        } catch (IOException e) {
            LOGGER.severe("Problem starting SOLR");
        }
    }

    private void processOnAmazon() {
        if (Project.getCurrentProject().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please create or open a project first");
            return;
        }
    }
}
