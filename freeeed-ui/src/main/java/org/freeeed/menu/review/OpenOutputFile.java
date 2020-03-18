package org.freeeed.menu.review;

import org.freeeed.services.Project;
import org.freeeed.services.Review;
import org.freeeed.ui.FreeEedUI;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class OpenOutputFile implements ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeEedUI.class);
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!areResultsPresent()) {
            return;
        }
        String resultsFolder = Project.getCurrentProject().getResultsDir();
        try {
            // Desktop should work, but it stopped lately in Ubuntu
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(resultsFolder));
            } else if (OsUtil.isLinux()) {
                String command = "nautilus " + resultsFolder;
                OsUtil.runCommand(command);
            } else if (OsUtil.isMac()) {
                String command = "open " + resultsFolder;
                OsUtil.runCommand(command);
            }
        } catch (IOException ex) {
            LOGGER.error("error OS util ", ex);
        }
    }

    private boolean areResultsPresent() {

        Project project = Project.getCurrentProject();
        if (project == null || project.isEmpty()) {
            JOptionPane.showMessageDialog(FreeEedUI.getInstance(), "Please open a project first");
            return false;
        }
        try {
            boolean success = Review.deliverFiles();
            if (!success) {
                JOptionPane.showMessageDialog(FreeEedUI.getInstance(), "No results yet");
                return false;
            }
        } catch (IOException e) {
            LOGGER.warn("Problem while checking for results", e);
            return false;
        }
        return true;
    }
}
