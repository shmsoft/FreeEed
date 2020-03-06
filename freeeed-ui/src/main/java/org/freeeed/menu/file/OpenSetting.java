package org.freeeed.menu.file;

import org.freeeed.ui.FreeEedUI;
import org.freeeed.ui.ProgramSettingsUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenSetting implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        ProgramSettingsUI programSettingsUI = new ProgramSettingsUI(FreeEedUI.getInstance(), true);
        programSettingsUI.setVisible(true);
    }
}
