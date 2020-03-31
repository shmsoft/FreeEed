package org.freeeed.menu.analytic;

import org.freeeed.services.Project;
import org.freeeed.ui.DocSimDlg;
import org.freeeed.ui.FreeEedUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenSmokingGun implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Project.getCurrentProject();
        if (project.isEmpty()) {
            JOptionPane.showMessageDialog(FreeEedUI.getInstance(), "Please select a project first");
            return;
        }
        DocSimDlg dlg = new DocSimDlg();
        dlg.setVisible(true);
    }
}
