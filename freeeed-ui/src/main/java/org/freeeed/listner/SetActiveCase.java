package org.freeeed.listner;

import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Project;
import org.freeeed.ui.FreeEedUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Map;

public class SetActiveCase implements ListSelectionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedUI.class);
    private JTable caseTable;
    private Map<Integer, Project> projects;

    public SetActiveCase(JTable caseTable) {
        this.caseTable = caseTable;
        try {
            this.projects = DbLocalUtils.getProjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
       if(e.getValueIsAdjusting()){
           try {
               openProject();
           } catch (Exception ex) {
               ex.printStackTrace();
           }
       }
    }


    private void openProject() throws Exception {
        int row = caseTable.getSelectedRow();
        projects = DbLocalUtils.getProjects();
        if (row >= 0) {
            int projectId = Integer.parseInt(caseTable.getValueAt(row, 0).toString().trim());
            Project project = projects.get(projectId);
            Project.setCurrentProject(project);
            LOGGER.debug("Opening project {} - {}", projectId,project.getProjectName());
            FreeEedUI.getInstance().setStatusBarProjectName(project.getProjectName());
        }
    }


}
