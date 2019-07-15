package org.freeeed.ui;

import com.google.common.io.Files;
import org.freeeed.services.Project;
import org.freeeed.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DocSimDlg extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton searchButton;
    private JTextArea docText;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocSimDlg.class);

    public DocSimDlg() {
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

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doSearch();
            }
        });
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
        DocSimDlg dialog = new DocSimDlg();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
    public void setVisible(boolean v) {
        if (v) {
            this.setLocationRelativeTo(null);
            setSize(600, 400);
            super.setVisible(v);
        }
    }
    private void doSearch() {
        LOGGER.info("Searching for the smoking gun");
        try {
            String inputText = docText.getText();
            // write the input text into a document
            File docFile = new File("/tmp/docfile.txt");
            LOGGER.info("Search for this: " + inputText);
            Files.write(inputText.getBytes(), docFile);
            List <String> pwd = OsUtil.runCommand("pwd");
            Project project = Project.getCurrentProject();
            String corpusDir = pwd.get(0) + "/" + project.getResultsDir() +
                    "/native/text";
            String python = "/home/mark/anaconda3/bin/python";
            String cmd = python + " " +
                    "/home/mark/projects/SHMsoft/docsimilarity/docsimilarity.py" +
                    " " + corpusDir +
                    " " + docFile;
            //List<String> andTheAnswerIs =  OsUtil.runCommand(cmd, true);
            LOGGER.info("Ready for the results?");
//            for (String s: andTheAnswerIs) {
//                LOGGER.info(s);
//            }
            LOGGER.info("Do you like the results?");
            docText.setText(cmd);
        } catch (IOException ex) {
            LOGGER.error("Ooops...", ex);
        }
    }
}
