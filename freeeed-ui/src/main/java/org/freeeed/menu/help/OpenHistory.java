package org.freeeed.menu.help;

import org.freeeed.ui.HistoryUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenHistory implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        HistoryUI ui = new HistoryUI();
        ui.setVisible(true);
    }
}
