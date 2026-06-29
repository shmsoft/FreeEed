package org.freeeed.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;

public class ControlPanelUI extends JFrame {

    private JButton startAllBtn;
    private JButton stopAllBtn;
    private JButton openUIBtn;
    private JLabel statusLabel;
    
    private JLabel solrIndicator;
    private JLabel aiIndicator;
    private JLabel uiIndicator;

    private final Color COLOR_START = new Color(46, 164, 79); // Soft Green
    private final Color COLOR_START_HOVER = new Color(44, 151, 75); 
    private final Color COLOR_STOP = new Color(203, 36, 49); // Soft Red
    private final Color COLOR_STOP_HOVER = new Color(180, 30, 40);
    private final Color COLOR_OPEN = new Color(3, 102, 214); // Soft Blue
    private final Color COLOR_OPEN_HOVER = new Color(0, 92, 197);
    private final Color COLOR_DISABLED = new Color(200, 200, 200);

    public ControlPanelUI() {
        super("FreeEed Manager");
        setupUI();
        pack();
        setLocationRelativeTo(null); // Center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(16, 20, 0, 20));
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 6, 0); // Tightened title gap

        JLabel titleLabel = new JLabel("FreeEed Service Manager", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        contentPane.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 12, 0); // Gap between buttons
        startAllBtn = new JButton("Start All Services");
        startAllBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startAllBtn.setForeground(Color.WHITE);
        startAllBtn.setPreferredSize(new Dimension(0, 50));
        addHoverEffect(startAllBtn, COLOR_START, COLOR_START_HOVER);
        startAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript("start_all");
            }
        });
        contentPane.add(startAllBtn, gbc);

        gbc.gridy++;
        stopAllBtn = new JButton("Stop All Services");
        stopAllBtn.setFont(new Font("Arial", Font.BOLD, 14));
        stopAllBtn.setForeground(Color.WHITE);
        stopAllBtn.setPreferredSize(new Dimension(0, 40));
        addHoverEffect(stopAllBtn, COLOR_STOP, COLOR_STOP_HOVER);
        stopAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runScript("stop_all");
            }
        });
        contentPane.add(stopAllBtn, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 0, 0); // No bottom inset for last button
        openUIBtn = new JButton("Open FreeEed UI");
        openUIBtn.setFont(new Font("Arial", Font.BOLD, 14));
        openUIBtn.setForeground(Color.WHITE);
        openUIBtn.setPreferredSize(new Dimension(0, 40));
        addHoverEffect(openUIBtn, COLOR_OPEN, COLOR_OPEN_HOVER);
        openUIBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWebUI();
            }
        });
        contentPane.add(openUIBtn, gbc);

        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 32, 20)); // Increased bottom padding (20 top, 32 bottom)

        statusLabel = new JLabel("Status: Ready", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        JPanel indicatorsPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        solrIndicator = new JLabel("● Solr", SwingConstants.CENTER);
        aiIndicator = new JLabel("● AI Advisor", SwingConstants.CENTER);
        uiIndicator = new JLabel("● Review UI", SwingConstants.CENTER);
        
        solrIndicator.setFont(new Font("Arial", Font.PLAIN, 11));
        aiIndicator.setFont(new Font("Arial", Font.PLAIN, 11));
        uiIndicator.setFont(new Font("Arial", Font.PLAIN, 11));

        indicatorsPanel.add(solrIndicator);
        indicatorsPanel.add(aiIndicator);
        indicatorsPanel.add(uiIndicator);
        
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        statusPanel.add(indicatorsPanel, BorderLayout.SOUTH);

        add(contentPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        
        setRunningState(false);
        updateStatus("Status: Ready", "ready");
    }
    
    private void addHoverEffect(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(normal);
                }
            }
        });
    }
    
    private void setRunningState(boolean running) {
        startAllBtn.setEnabled(!running);
        stopAllBtn.setEnabled(running);
        openUIBtn.setEnabled(running);
        
        startAllBtn.setBackground(running ? COLOR_DISABLED : COLOR_START);
        stopAllBtn.setBackground(running ? COLOR_STOP : COLOR_DISABLED);
        openUIBtn.setBackground(running ? COLOR_OPEN : COLOR_DISABLED);
        
        Color indicatorColor = running ? COLOR_START : new Color(150, 150, 150);
        solrIndicator.setForeground(indicatorColor);
        aiIndicator.setForeground(indicatorColor);
        uiIndicator.setForeground(indicatorColor);
    }
    
    private void updateStatus(String text, String state) {
        statusLabel.setText(text);
        switch (state.toLowerCase()) {
            case "starting":
                statusLabel.setForeground(new Color(215, 138, 51)); // Orange
                break;
            case "failed":
                statusLabel.setForeground(COLOR_STOP); // Red
                break;
            case "ready":
            default:
                statusLabel.setForeground(COLOR_START); // Green
                break;
        }
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
            updateStatus("Status: Error - Script not found: " + scriptName, "failed");
            return;
        }

        try {
            updateStatus("Status: Starting " + scriptName + "...", "starting");
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd.exe", "/c", scriptFile.getAbsolutePath());
            } else {
                pb = new ProcessBuilder("sh", scriptFile.getAbsolutePath());
            }
            pb.directory(scriptFile.getParentFile());
            pb.start();
            
            if (baseName.equals("start_all")) {
                setRunningState(true);
                updateStatus("Status: Services running", "ready");
            } else if (baseName.equals("stop_all")) {
                setRunningState(false);
                updateStatus("Status: Ready", "ready");
            }
            
        } catch (IOException ex) {
            updateStatus("Status: Error executing script", "failed");
            ex.printStackTrace();
        }
    }

    private void openWebUI() {
        try {
            String url = org.freeeed.services.Settings.getSettings().getReviewEndpoint();
            Desktop.getDesktop().browse(new URI(url));
            updateStatus("Status: Opened UI in browser", "ready");
        } catch (Exception ex) {
            updateStatus("Status: Failed to open browser", "failed");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Set modern flat look and feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
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
