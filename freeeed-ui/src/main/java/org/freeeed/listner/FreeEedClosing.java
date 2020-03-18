package org.freeeed.listner;

import org.freeeed.services.Settings;
import org.freeeed.ui.FreeEedUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FreeEedClosing extends WindowAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedUI.class);

    @Override
    public void windowClosing(WindowEvent e) {
        try {
            Settings.getSettings().save();
        } catch (Exception ex) {
            LOGGER.error("Error saving project", ex);
            JOptionPane.showMessageDialog(null, "Application error " + ex.getMessage());
        }
    }
}
