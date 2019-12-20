package org.freeeed.menu.file;

import org.freeeed.ui.FreeEedUI;
import org.freeeed.ui.S3SetupUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenS3Setting implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        S3SetupUI ui = new S3SetupUI(FreeEedUI.getInstance());
        ui.setVisible(true);
    }
}
