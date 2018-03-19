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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mark
 */
public class ProjectsUI extends javax.swing.JDialog {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsUI.class);
    private static final String[] columns = new String[]{
        "Project ID", "Name", "Date created"
    };
    private Map<Integer, Project> projects = null;

    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;

    /**
     * Creates new form ProjectsUI
     *  @param parent
     *
     */
    public ProjectsUI(Frame parent) {
        super(parent, true);
        initComponents();

        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
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

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        projectScrollPane = new javax.swing.JScrollPane();
        projectTable = new javax.swing.JTable();
        editProjectButton = new javax.swing.JButton();

        setTitle("FreeEed projects");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());
        getContentPane().setPreferredSize(new Dimension(1000, 300));

        okButton.setText("Select");
        okButton.addActionListener(this::okButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 16, 15);
        getContentPane().add(okButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 261, 16, 0);
        getContentPane().add(cancelButton, gridBagConstraints);

        newButton.setText("New");
        newButton.addActionListener(this::newButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 15, 16, 0);
        getContentPane().add(newButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        deleteButton.setText("Delete");
        deleteButton.addActionListener(this::deleteButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 15, 16, 0);
        getContentPane().add(deleteButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        projectTable.setModel(new DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Project ID", "Name", "Date created"
            }
        ) {
            Class[] types = new Class [] {
                String.class, String.class, Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        projectTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                projectTableMouseClicked(evt);
            }
        });
        projectTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                projectTableKeyPressed(evt);
            }
        });
        projectScrollPane.setViewportView(projectTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 636;
        gridBagConstraints.ipady = 282;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 15, 0, 15);
        getContentPane().add(projectScrollPane, gridBagConstraints);

        editProjectButton.setText("Edit");
        editProjectButton.addActionListener(this::editProjectButtonActionPerformed);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(9, 9, 16, 0);
        getContentPane().add(editProjectButton, gridBagConstraints);
        getRootPane().setDefaultButton(okButton);

        pack();
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        try {
            openProject();
        } catch (Exception e) {
            LOGGER.error("Problem opening project", e);
        }
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

    private void newButtonActionPerformed(ActionEvent evt) {
        try {
            newProject();
        } catch (Exception e) {
            LOGGER.error("Problem creating new project", e);
        }
    }

    private void deleteButtonActionPerformed(ActionEvent evt) {
        try {
            deleteProject();
        } catch (Exception e) {
            LOGGER.error("Problem deleting project", e);
        }
    }

    private void projectTableKeyPressed(KeyEvent evt) {
        try {
            openWithKeyPress(evt);
        } catch (Exception e) {
            LOGGER.error("Problem opening project", e);
        }
    }

    private void projectTableMouseClicked(MouseEvent evt) {
        try {
            openWithDoubleClick(evt);
        } catch (Exception e) {
            LOGGER.error("Problem opening project", e);
        }
    }
    private void editProjectButtonActionPerformed(ActionEvent evt) {
        try {
            openProjectForEditing();
        } catch (Exception e) {
            LOGGER.error("Problem opening project for editing");
        }        
    }
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editProjectButton;
    private javax.swing.JButton newButton;
    private javax.swing.JButton okButton;
    private javax.swing.JScrollPane projectScrollPane;
    private javax.swing.JTable projectTable;
    private int returnStatus = RET_CANCEL;
    
    private void showProjectTableData() throws Exception {
        projectTable.setModel(new DefaultTableModel(getProjectTableData(), columns) {
            Class[] types = new Class[]{
                String.class, String.class, String.class
            };
            boolean[] canEdit = new boolean[]{
                false, false, false
            };
            
            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
            
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
    }
    
    @Override
    public void setVisible(boolean b) {
        try {
            if (b) {
                myInit();
            }
            super.setVisible(b);
        } catch (Exception e) {
            LOGGER.error("Internal db problem", e);
            JOptionPane.showMessageDialog(getParent(),
                    "Problem with internal database", "Sorry", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void myInit() throws Exception {
        showProjectTableData();
    }
    
    private Object[][] getProjectTableData() throws Exception {
        projects = DbLocalUtils.getProjects();
        Set<Integer> keys = projects.keySet();
        List<Integer> list = new ArrayList(keys);
        Collections.sort(list);
        Object[][] data = new Object[projects.size()][3];
        int row = 0;
        for (int projectId : list) {
            Project project = projects.get(projectId);
            data[row][0] = projectId;
            data[row][1] = project.getProjectName();
            data[row][2] = project.getCreated();
            row++;
        }
        return data;
    }
    
    private void openProjectForEditing() throws Exception {
        int row = projectTable.getSelectedRow();
        if (row >= 0) {
            int projectId = (Integer) projectTable.getValueAt(row, 0);
            Project project = projects.get(projectId);
            Project.setCurrentProject(project);
            LOGGER.debug("Opening project {}", projectId);
            doClose(RET_OK);
            FreeEedUI.getInstance().showProcessingOptions();
        }
    }
    
    private void openProject() throws Exception {
        int row = projectTable.getSelectedRow();
        if (row >= 0) {
            int projectId = (Integer) projectTable.getValueAt(row, 0);
            Project project = projects.get(projectId);
            Project.setCurrentProject(project);
            LOGGER.debug("Opening project {}", projectId);
            doClose(RET_OK);
            FreeEedUI.getInstance().updateTitle(project.getProjectName());
        }
    }
    
    private void openWithKeyPress(KeyEvent evt) throws Exception {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            openProject();
        }
    }
    
    private void openWithDoubleClick(MouseEvent evt) throws Exception {
        if (evt.getClickCount() == 2) {
            openProjectForEditing();
        }
    }
    
    private void deleteProject() throws Exception {
        int row = projectTable.getSelectedRow();
        if (row >= 0) {
            int projectId = (Integer) projectTable.getValueAt(row, 0);
            int retStatus = JOptionPane.showConfirmDialog(this, "Delete project " + projectId + "?");
            if (retStatus == JOptionPane.OK_OPTION) {
                DbLocalUtils.deleteProject(projectId);
                showProjectTableData();
            }
            LOGGER.debug("Deleted project {}", projectId);
        }
    }
    
    private void newProject() throws Exception {
        Project project = DbLocalUtils.createNewProject();
        Project.setCurrentProject(project);
        LOGGER.debug("Opening project {}", project.getProjectCode());
        doClose(RET_OK);
        FreeEedUI.getInstance().showProcessingOptions();
    }
}
