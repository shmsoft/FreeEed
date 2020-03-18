package org.freeeed.menu.file;

import org.freeeed.services.Settings;
import org.freeeed.ui.FreeEedUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitApplication implements ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedUI.class);

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Settings.getSettings().save();
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.error("Error saving project", e);
            JOptionPane.showMessageDialog(FreeEedUI.getInstance(), "Application error " + ex.getMessage());
        }
    }
}
