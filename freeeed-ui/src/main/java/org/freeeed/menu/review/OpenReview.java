package org.freeeed.menu.review;

import org.freeeed.services.Settings;
import org.freeeed.ui.FreeEedUI;
import org.freeeed.ui.UtilUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenReview implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {

        Settings settings = Settings.getSettings();
        String url = settings.getReviewEndpoint();
        UtilUI.openBrowser(FreeEedUI.getInstance(), url);


    }
}
