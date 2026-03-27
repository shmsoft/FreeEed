package org.freeeed.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class ControlPanelUI extends JFrame {

    private JButton startAllBtn;
    private JButton stopAllBtn;
    private JButton openUIBtn;
    private JLabel statusLabel;

    public ControlPanelUI() {
        super("FreeEed Control Panel");
        setupUI();
        pack();
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPane.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel titleLabel = new JLabel("FreeEed Service Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPane.add(titleLabel);

        startAllBtn = new JButton("Start All Services");
        startAllBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        startAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript("start_all");
            }
        });
        contentPane.add(startAllBtn);

        stopAllBtn = new JButton("Stop All Services");
        stopAllBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        stopAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript("stop_all");
            }
        });
        contentPane.add(stopAllBtn);

        openUIBtn = new JButton("Open FreeEed UI");
        openUIBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        openUIBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWebUI();
            }
        });
        contentPane.add(openUIBtn);

        statusLabel = new JLabel("Status: Ready", SwingConstants.CENTER);

        add(contentPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void runScript(String baseName) {
        String os = System.getProperty("os.name").toLowerCase();
        String scriptName = baseName + (os.contains("win") ? ".bat" : ".sh");
        File scriptFile = new File(System.getProperty("user.dir"), scriptName);

        if (!scriptFile.exists()) {
            // If running inside freeeed-processing, the scripts are one level up
            scriptFile = new File(System.getProperty("user.dir") + "/..", scriptName);
        }

        if (!scriptFile.exists()) {
            statusLabel.setText("Status: Error - Script not found: " + scriptName);
            return;
        }

        try {
            statusLabel.setText("Status: Running " + scriptName + "...");
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c", scriptFile.getAbsolutePath());
            } else {
                pb = new ProcessBuilder("sh", scriptFile.getAbsolutePath());
            }
            pb.directory(scriptFile.getParentFile());
            pb.start();
            statusLabel.setText("Status: " + baseName + " initiated");
        } catch (IOException ex) {
            statusLabel.setText("Status: Error executing script");
            ex.printStackTrace();
        }
    }

    private void openWebUI() {
        try {
            String url = "http://localhost:8080/freeeedui"; // verify url?
            Desktop.getDesktop().browse(new java.net.URI(url));
            statusLabel.setText("Status: Opened UI in browser");
        } catch (Exception ex) {
            statusLabel.setText("Status: Failed to open browser");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ControlPanelUI().setVisible(true);
            }
        });
    }
}
