package com.jobrec;

import com.jobrec.controller.JobRecruitmentController;
import com.jobrec.view.SwingView;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Application Entry Point.
// Instantiates the Controller, passes it to the SwingView,
// and starts the desktop GUI safely on the Event Dispatch Thread (EDT).
public class Main {
    public static void main(String[] args) {
        // Set system look and feel for native UI rendering if available
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            JobRecruitmentController controller = new JobRecruitmentController();
            SwingView view = new SwingView(controller);
            view.setVisible(true);
        });
    }
}
