package com.jiracopier;

import com.jiracopier.gui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a modern look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Warning: Could not set the system look and feel.");
            }
            new MainFrame().setVisible(true);
        });
    }
}
