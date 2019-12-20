package org.freeeed.menu.help;

import org.freeeed.ui.AboutDialog;
import org.freeeed.ui.FreeEedUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenAbout implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        new AboutDialog(FreeEedUI.getInstance()).setVisible(true);
    }
}
