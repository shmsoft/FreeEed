package org.freeeed.menu.analytic;

import org.freeeed.services.Project;
import org.freeeed.ui.FreeEedUI;
import org.freeeed.ui.WordCloudUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenWordCloud implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {

        Project project = Project.getCurrentProject();

        if (project.isEmpty()) {
            JOptionPane.showMessageDialog(FreeEedUI.getInstance(), "Please select a project first");
            return;
        }

        WordCloudUI ui = new WordCloudUI(FreeEedUI.getInstance(), true);
        ui.setVisible(true);

    }
}
