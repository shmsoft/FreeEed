package org.freeeed.ui;

import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.Version;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AboutGUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea aboutText;
    private JFrame parentFrame;

    public AboutGUI(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        AboutGUI dialog = new AboutGUI(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
    private void myInitComponents() {
        String aboutTextStr =
                Version.getVersionAndBuild()
                        + "\n"
                        + "\n"
                        + "FreeEed" + ParameterProcessing.TM + " Player"
                        + "\n"
                        + "For additional information, please visit www.freeeed.org"
                        + "\n"
                        + "\n"
                        + "Brought to you by the FreeEed" + ParameterProcessing.TM + " team";

        aboutText.setText(aboutTextStr);
        aboutText.setEditable(false);
        setTitle("About " + ParameterProcessing.APP_NAME);
        setLocationRelativeTo(parentFrame);
        setPreferredSize(new Dimension(1000, 500));
    }

    @Override
    public void setVisible(boolean b) {
        myInitComponents();
        super.setVisible(b);
    }
}
