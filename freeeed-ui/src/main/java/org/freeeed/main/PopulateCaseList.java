package org.freeeed.main;

import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PopulateCaseList {

    public static void Populate(JTable caseTable) {
        final String[] columns = new String[]{"Case ID", "Scaia ID", "Name", "Date created", ""};
        Map<Integer, Project> projects = null;
        try {
            projects = DbLocalUtils.getProjects();
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<Integer> list = new ArrayList(projects.keySet());
        Object[][] data = new Object[projects.size()][4];
        int row = 0;
        for (int projectId : list) {
            Project project = projects.get(projectId);
            data[row][0] = projectId;
            data[row][1] = projectId;
            data[row][2] = "  " + project.getProjectName();
            data[row][3] = "  " + project.getCreated();
            row++;
        }

        caseTable.setModel(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        });
    }


}
