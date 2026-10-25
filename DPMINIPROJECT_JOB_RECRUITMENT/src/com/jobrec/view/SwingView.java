package com.jobrec.view;

import com.jobrec.controller.JobRecruitmentController;
import com.jobrec.memento.ProfileHistory;
import com.jobrec.model.Candidate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

// MVC — the SWING VIEW.
// Desktop graphical user interface for the Job Search & Recruitment System.
// Completely separates UI interaction from business logic and database access.
public class SwingView extends JFrame {

    private final JobRecruitmentController controller;
    private JTextArea outputArea;
    private JLabel statusBar;

    public SwingView(JobRecruitmentController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Job Search & Recruitment Management System (Java Swing + SQLite)");
        setSize(1050, 720);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // -------------------------------------------------------------
        // North Panel: Title Header & Utilities
        // -------------------------------------------------------------
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 5, 10, 5)
        ));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 2, 2));
        JLabel titleLabel = new JLabel("Job Recruitment Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(24, 43, 73));

        JLabel subtitleLabel = new JLabel("MVC Architecture • 14 Design Patterns • SQLite Database Persistence");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 110, 125));

        titleBlock.add(titleLabel);
        titleBlock.add(subtitleLabel);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JButton btnClear = new JButton("Clear Log");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.addActionListener(e -> outputArea.setText(""));
        headerButtons.add(btnClear);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(headerButtons, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // -------------------------------------------------------------
        // Left Panel (Navigation): 10 Buttons in a Sidebar
        // -------------------------------------------------------------
        JPanel sideNavPanel = new JPanel(new BorderLayout());
        sideNavPanel.setPreferredSize(new Dimension(270, 0));
        sideNavPanel.setBorder(BorderFactory.createTitledBorder("Navigation & Patterns"));

        JPanel buttonsGrid = new JPanel(new GridLayout(10, 1, 6, 6));
        buttonsGrid.setBorder(new EmptyBorder(8, 8, 8, 8));

        JButton btnRegister = createNavButton("1. Register Candidate [Builder]");
        JButton btnPostJob = createNavButton("2. Post Job [Abstract Factory]");
        JButton btnViewCandidates = createNavButton("3. View Candidates [Proxy]");
        JButton btnViewJobs = createNavButton("4. View Jobs [Database]");
        JButton btnSearchJobs = createNavButton("5. Search Jobs [Strategy]");
        JButton btnApply = createNavButton("6. Apply for Job [Chain/Bridge/Obs]");
        JButton btnDuplicate = createNavButton("7. Duplicate Job [Prototype]");
        JButton btnEditProfile = createNavButton("8. Edit Profile [Memento]");
        JButton btnWorkflow = createNavButton("9. Interview Workflow [Template]");
        JButton btnMediator = createNavButton("10. Department Routing [Mediator]");

        buttonsGrid.add(btnRegister);
        buttonsGrid.add(btnPostJob);
        buttonsGrid.add(btnViewCandidates);
        buttonsGrid.add(btnViewJobs);
        buttonsGrid.add(btnSearchJobs);
        buttonsGrid.add(btnApply);
        buttonsGrid.add(btnDuplicate);
        buttonsGrid.add(btnEditProfile);
        buttonsGrid.add(btnWorkflow);
        buttonsGrid.add(btnMediator);

        sideNavPanel.add(buttonsGrid, BorderLayout.CENTER);
        mainPanel.add(sideNavPanel, BorderLayout.WEST);

        // -------------------------------------------------------------
        // Center Panel (Display): JTextArea inside JScrollPane
        // -------------------------------------------------------------
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("System Console & Pattern Output"));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        outputArea.setBackground(new Color(28, 30, 36));
        outputArea.setForeground(new Color(225, 230, 240));
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setMargin(new Insets(10, 12, 10, 12));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // -------------------------------------------------------------
        // South Panel: Status Bar
        // -------------------------------------------------------------
        statusBar = new JLabel(" Ready | Connected to SQLite Database (jobrecruitment.db)");
        statusBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusBar.setForeground(new Color(80, 80, 80));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                new EmptyBorder(5, 5, 2, 5)
        ));
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // -------------------------------------------------------------
        // Wire Action Listeners
        // -------------------------------------------------------------
        btnRegister.addActionListener(this::handleRegisterCandidate);
        btnPostJob.addActionListener(this::handlePostJob);
        btnViewCandidates.addActionListener(this::handleViewCandidates);
        btnViewJobs.addActionListener(this::handleViewJobs);
        btnSearchJobs.addActionListener(this::handleSearchJobs);
        btnApply.addActionListener(this::handleApplyForJob);
        btnDuplicate.addActionListener(this::handleDuplicateJob);
        btnEditProfile.addActionListener(this::handleEditProfile);
        btnWorkflow.addActionListener(this::handleRunWorkflow);
        btnMediator.addActionListener(this::handleDepartmentRouting);

        // Welcome text on launch
        log("=== Welcome to the Job Search & Recruitment Management System ===");
        log("GUI successfully loaded with Java Swing and strict MVC pattern.");
        log("Database connected: SQLite via JDBC (Singleton pattern active).");
        log("Click any button on the left sidebar to execute workflows and inspect design pattern outputs.\n");
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(4, 10, 4, 10));
        return btn;
    }

    private void log(String message) {
        outputArea.append(message + "\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void updateStatus(String status) {
        statusBar.setText(" " + status);
    }

    // =====================================================================
    // 1. Register Candidate [Builder Pattern]
    // =====================================================================
    private void handleRegisterCandidate(ActionEvent e) {
        JTextField nameField = new JTextField("Alice Developer");
        JTextField ageField = new JTextField("25");
        JTextField eduField = new JTextField("B.Tech CSE");
        JTextField skillsField = new JTextField("Java, Spring, SQL");
        JTextField expField = new JTextField("2 years");
        JTextField locField = new JTextField("New York");
        JTextField salField = new JTextField("100000");

        JPanel form = new JPanel(new GridLayout(7, 2, 6, 6));
        form.add(new JLabel("Full Name:")); form.add(nameField);
        form.add(new JLabel("Age:")); form.add(ageField);
        form.add(new JLabel("Education:")); form.add(eduField);
        form.add(new JLabel("Skills (comma-separated):")); form.add(skillsField);
        form.add(new JLabel("Experience (e.g. 2 years):")); form.add(expField);
        form.add(new JLabel("Preferred Location:")); form.add(locField);
        form.add(new JLabel("Expected Salary ($):")); form.add(salField);

        int res = JOptionPane.showConfirmDialog(this, form, "Register Candidate (Builder Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String edu = eduField.getText().trim();
                String skills = skillsField.getText().trim();
                String exp = expField.getText().trim();
                String loc = locField.getText().trim();
                double sal = Double.parseDouble(salField.getText().trim());

                String result = controller.registerCandidate(name, age, edu, skills, exp, loc, sal);
                log("\n" + result + "\n");
                updateStatus("Candidate registered successfully.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Age and Salary must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 2. Post Job [Abstract Factory Pattern]
    // =====================================================================
    private void handlePostJob(ActionEvent e) {
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"IT", "Finance", "Marketing"});
        JTextField titleField = new JTextField("Senior Java Engineer");
        JTextField skillsField = new JTextField("Java, SQL");
        JTextField locField = new JTextField("New York");
        JTextField salField = new JTextField("110000");

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 6));
        form.add(new JLabel("Category:")); form.add(categoryBox);
        form.add(new JLabel("Job Title:")); form.add(titleField);
        form.add(new JLabel("Required Skills:")); form.add(skillsField);
        form.add(new JLabel("Location:")); form.add(locField);
        form.add(new JLabel("Offered Salary ($):")); form.add(salField);

        int res = JOptionPane.showConfirmDialog(this, form, "Post New Job (Abstract Factory)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                String category = (String) categoryBox.getSelectedItem();
                String title = titleField.getText().trim();
                String skills = skillsField.getText().trim();
                String loc = locField.getText().trim();
                double sal = Double.parseDouble(salField.getText().trim());

                String result = controller.postJob(category, title, skills, loc, sal);
                log("\n" + result + "\n");
                updateStatus("Job posted in category " + category);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Salary must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 3. View Candidates [Proxy Pattern: Role Access Gate]
    // =====================================================================
    private void handleViewCandidates(ActionEvent e) {
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"RECRUITER", "CANDIDATE", "GUEST"});
        JTextField idField = new JTextField("1");

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Select Role:")); form.add(roleBox);
        form.add(new JLabel("Candidate ID:")); form.add(idField);

        int res = JOptionPane.showConfirmDialog(this, form, "View Candidate (Proxy Security Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                String role = (String) roleBox.getSelectedItem();
                int id = Integer.parseInt(idField.getText().trim());

                String result = controller.getCandidateDetails(role, id);
                log("\n" + result + "\n");
                updateStatus("View candidate query completed for role: " + role);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Candidate ID must be an integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 4. View Jobs
    // =====================================================================
    private void handleViewJobs(ActionEvent e) {
        String result = controller.getAllJobsFormatted();
        log("\n" + result);
        updateStatus("Loaded all active job postings.");
    }

    // =====================================================================
    // 5. Search Jobs [Strategy Pattern]
    // =====================================================================
    private void handleSearchJobs(ActionEvent e) {
        JTextField idField = new JTextField("1");
        JComboBox<String> stratBox = new JComboBox<>(new String[]{"Skill-Heavy (Default)", "Experience-Heavy"});

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Candidate ID:")); form.add(idField);
        form.add(new JLabel("Matching Strategy:")); form.add(stratBox);

        int res = JOptionPane.showConfirmDialog(this, form, "Search Matching Jobs (Strategy Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String strat = (String) stratBox.getSelectedItem();

                String result = controller.searchJobs(id, strat);
                log("\n" + result + "\n");
                updateStatus("Strategy match completed using " + strat);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Candidate ID must be an integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 6. Apply for Job [Chain + Observer + Bridge]
    // =====================================================================
    private void handleApplyForJob(ActionEvent e) {
        JTextField candField = new JTextField("1");
        JTextField jobField = new JTextField("1");
        JComboBox<String> channelBox = new JComboBox<>(new String[]{"EMAIL", "SMS"});
        JTextField contactField = new JTextField("alice@example.com");

        channelBox.addActionListener(ev -> {
            if ("SMS".equals(channelBox.getSelectedItem())) {
                contactField.setText("9876543210");
            } else {
                contactField.setText("alice@example.com");
            }
        });

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Candidate ID:")); form.add(candField);
        form.add(new JLabel("Job ID:")); form.add(jobField);
        form.add(new JLabel("Notification Channel:")); form.add(channelBox);
        form.add(new JLabel("Contact Address / Phone:")); form.add(contactField);

        int res = JOptionPane.showConfirmDialog(this, form, "Apply for Job (Chain of Responsibility + Observer + Bridge)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                int candId = Integer.parseInt(candField.getText().trim());
                int jobId = Integer.parseInt(jobField.getText().trim());
                String channel = (String) channelBox.getSelectedItem();
                String contact = contactField.getText().trim();

                String result = controller.applyForJob(candId, jobId, channel, contact);
                log("\n" + result + "\n");
                updateStatus("Job application pipeline executed.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Candidate ID and Job ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 7. Duplicate Job [Prototype Pattern]
    // =====================================================================
    private void handleDuplicateJob(ActionEvent e) {
        JTextField idField = new JTextField("1");
        JTextField titleField = new JTextField("Lead Java Engineer");
        JTextField salField = new JTextField("145000");
        JTextField locField = new JTextField("New York");

        JPanel form = new JPanel(new GridLayout(4, 2, 6, 6));
        form.add(new JLabel("Original Job ID to Clone:")); form.add(idField);
        form.add(new JLabel("New Title:")); form.add(titleField);
        form.add(new JLabel("New Salary ($):")); form.add(salField);
        form.add(new JLabel("New Location:")); form.add(locField);

        int res = JOptionPane.showConfirmDialog(this, form, "Duplicate Job Posting (Prototype Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String title = titleField.getText().trim();
                double sal = Double.parseDouble(salField.getText().trim());
                String loc = locField.getText().trim();

                String result = controller.duplicateJob(id, title, sal, loc);
                log("\n" + result + "\n");
                updateStatus("Job duplicated via Prototype pattern.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Job ID and Salary must be valid numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 8. Edit Profile [Memento Pattern: Undo Support]
    // =====================================================================
    private void handleEditProfile(ActionEvent e) {
        String inputId = JOptionPane.showInputDialog(this, "Enter Candidate ID to Edit Profile:", "1");
        if (inputId == null || inputId.trim().isEmpty()) return;

        try {
            int candidateId = Integer.parseInt(inputId.trim());
            Candidate candidate = controller.getCandidate(candidateId);
            if (candidate == null) {
                JOptionPane.showMessageDialog(this, "Candidate #" + candidateId + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create Memento Caretaker
            ProfileHistory history = new ProfileHistory();

            JDialog editDialog = new JDialog(this, "Edit Profile - Memento Pattern (Candidate #" + candidateId + ")", true);
            editDialog.setSize(520, 380);
            editDialog.setLocationRelativeTo(this);
            editDialog.setLayout(new BorderLayout(10, 10));

            JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 8, 8));
            fieldsPanel.setBorder(new EmptyBorder(12, 12, 8, 12));

            JTextField nameField = new JTextField(candidate.getName());
            nameField.setEditable(false);
            JTextField eduField = new JTextField(candidate.getEducation());
            JTextField skillsField = new JTextField(candidate.getSkills());
            JTextField expField = new JTextField(candidate.getExperience());
            JTextField locField = new JTextField(candidate.getLocation());
            JTextField salField = new JTextField(String.valueOf(candidate.getExpectedSalary()));

            fieldsPanel.add(new JLabel("Name (read-only):")); fieldsPanel.add(nameField);
            fieldsPanel.add(new JLabel("Education:")); fieldsPanel.add(eduField);
            fieldsPanel.add(new JLabel("Skills:")); fieldsPanel.add(skillsField);
            fieldsPanel.add(new JLabel("Experience:")); fieldsPanel.add(expField);
            fieldsPanel.add(new JLabel("Location:")); fieldsPanel.add(locField);
            fieldsPanel.add(new JLabel("Expected Salary:")); fieldsPanel.add(salField);

            editDialog.add(fieldsPanel, BorderLayout.CENTER);

            // Controls
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBorder(new EmptyBorder(6, 12, 12, 12));

            JLabel undoLabel = new JLabel("Undo History Stack: 0 snapshots");
            undoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));

            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            JButton btnSnapshot = new JButton("Snapshot Change");
            JButton btnUndo = new JButton("Undo (Memento)");
            JButton btnSave = new JButton("Save to Database");
            JButton btnClose = new JButton("Close");

            buttonsPanel.add(btnSnapshot);
            buttonsPanel.add(btnUndo);
            buttonsPanel.add(btnSave);
            buttonsPanel.add(btnClose);

            bottomPanel.add(undoLabel, BorderLayout.WEST);
            bottomPanel.add(buttonsPanel, BorderLayout.EAST);
            editDialog.add(bottomPanel, BorderLayout.SOUTH);

            btnSnapshot.addActionListener(ev -> {
                // Save current state into memento before applying new modifications
                history.save(candidate.saveToMemento());
                try {
                    candidate.setEducation(eduField.getText().trim());
                    candidate.setSkills(skillsField.getText().trim());
                    candidate.setExperience(expField.getText().trim());
                    candidate.setLocation(locField.getText().trim());
                    candidate.setExpectedSalary(Double.parseDouble(salField.getText().trim()));
                    undoLabel.setText("Undo History Stack: " + history.size() + " snapshots");
                    JOptionPane.showMessageDialog(editDialog, "Current profile state snapshotted into Memento!", "Snapshot Saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Salary must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnUndo.addActionListener(ev -> {
                if (history.isEmpty()) {
                    JOptionPane.showMessageDialog(editDialog, "No previous snapshots in Memento history stack.", "Undo Stack Empty", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Restore originator from Caretaker's memento
                candidate.restoreFromMemento(history.undo());
                eduField.setText(candidate.getEducation());
                skillsField.setText(candidate.getSkills());
                expField.setText(candidate.getExperience());
                locField.setText(candidate.getLocation());
                salField.setText(String.valueOf(candidate.getExpectedSalary()));
                undoLabel.setText("Undo History Stack: " + history.size() + " snapshots");
                log("[MEMENTO UNDO] Reverted candidate #" + candidate.getId() + " profile to previous snapshot.");
            });

            btnSave.addActionListener(ev -> {
                try {
                    candidate.setEducation(eduField.getText().trim());
                    candidate.setSkills(skillsField.getText().trim());
                    candidate.setExperience(expField.getText().trim());
                    candidate.setLocation(locField.getText().trim());
                    candidate.setExpectedSalary(Double.parseDouble(salField.getText().trim()));

                    boolean updated = controller.updateCandidateProfile(candidate);
                    if (updated) {
                        log("[MEMENTO SAVED] Candidate #" + candidate.getId() + " profile successfully updated in database:\n" + candidate);
                        JOptionPane.showMessageDialog(editDialog, "Profile updated in SQLite database successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        editDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(editDialog, "Failed to update candidate profile in database.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(editDialog, "Salary must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnClose.addActionListener(ev -> editDialog.dispose());

            editDialog.setVisible(true);
            updateStatus("Profile edit session ended.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Candidate ID must be an integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    // 9. Run Interview Workflow [Template Method Pattern]
    // =====================================================================
    private void handleRunWorkflow(ActionEvent e) {
        JTextField candField = new JTextField("1");
        JTextField jobField = new JTextField("1");

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Candidate ID:")); form.add(candField);
        form.add(new JLabel("Job ID:")); form.add(jobField);

        int res = JOptionPane.showConfirmDialog(this, form, "Run Category Interview Workflow (Template Method Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                int candId = Integer.parseInt(candField.getText().trim());
                int jobId = Integer.parseInt(jobField.getText().trim());

                String result = controller.runInterviewWorkflow(candId, jobId);
                log("\n" + result + "\n");
                updateStatus("Interview workflow executed.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Candidate ID and Job ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =====================================================================
    // 10. Department Routing [Mediator Pattern]
    // =====================================================================
    private void handleDepartmentRouting(ActionEvent e) {
        JTextField candField = new JTextField("1");
        JTextField jobField = new JTextField("1");

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Candidate ID:")); form.add(candField);
        form.add(new JLabel("Job ID:")); form.add(jobField);

        int res = JOptionPane.showConfirmDialog(this, form, "Department Coordination (Mediator Pattern)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            try {
                int candId = Integer.parseInt(candField.getText().trim());
                int jobId = Integer.parseInt(jobField.getText().trim());

                String result = controller.processViaDepartments(candId, jobId);
                log("\n" + result + "\n");
                updateStatus("Mediator routing completed.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Candidate ID and Job ID must be integers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
