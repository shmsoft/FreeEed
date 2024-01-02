package org.freeeed.ui;

import javax.swing.*;
import java.util.List;

public class BackgroundIndexing {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Background Task Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 100);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        JButton startButton = new JButton("Start Task");
        JButton cancelButton = new JButton("Cancel Task");

        SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    if (isCancelled()) {
                        return null; // Task was cancelled
                    }
                    Thread.sleep(100); // Simulate a long-running task
                    publish(i); // Publish the progress
                }
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                progressBar.setValue(progress);
            }
        };

        startButton.addActionListener(e -> worker.execute());
        cancelButton.addActionListener(e -> worker.cancel(true));

        frame.setLayout(new java.awt.FlowLayout());
        frame.add(progressBar);
        frame.add(startButton);
        frame.add(cancelButton);
        frame.setVisible(true);
    }
}
