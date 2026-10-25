package com.jobrec.view;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

import java.util.List;
import java.util.Scanner;

// MVC — the VIEW.
// All console input (Scanner) and output (System.out) for the main app
// lives here. The view has no business logic and never touches the DB.
public class ConsoleView {

    private final Scanner sc = new Scanner(System.in);

    // ---------- menus ----------
    public void showBanner() {
        System.out.println("=== Job Search & Recruitment Management System ===");
    }

    public String showMenuAndReadChoice() {
        System.out.println("\n1. Register Candidate");
        System.out.println("2. Post Job (Recruiter)");
        System.out.println("3. View Candidates");
        System.out.println("4. View Jobs");
        System.out.println("5. Search Matching Jobs for a Candidate");
        System.out.println("6. Apply for a Job (runs eligibility + approval chain + notifications)");
        System.out.println("7. Duplicate existing Job Posting (Recruiter)");
        System.out.println("8. Edit Candidate Profile (with undo)");
        System.out.println("9. Run Category Interview Workflow");
        System.out.println("10. Process Application via Departments");
        System.out.println("0. Exit");
        return readLine("Choose an option: ").trim();
    }

    public String showStrategyMenuAndReadChoice() {
        System.out.println("Matching strategy:");
        System.out.println("  1. Skill-Heavy (default)");
        System.out.println("  2. Experience-Heavy");
        return readLine("Choose strategy [1]: ").trim();
    }

    public String showProfileEditMenuAndReadChoice(int undoAvailable) {
        System.out.println("\nEdit profile:");
        System.out.println("  1. Skills");
        System.out.println("  2. Experience");
        System.out.println("  3. Education");
        System.out.println("  4. Preferred Location");
        System.out.println("  5. Expected Salary");
        System.out.println("  6. Undo last change (" + undoAvailable + " available)");
        System.out.println("  7. Save to database & exit");
        System.out.println("  0. Discard changes & exit");
        return readLine("Choose: ").trim();
    }

    // ---------- input helpers ----------
    public String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    public int readInt(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                showError("Please enter a whole number.");
            }
        }
    }

    public double readDouble(String prompt) {
        while (true) {
            String s = readLine(prompt).trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                showError("Please enter a number.");
            }
        }
    }

    // Blank input keeps the current value
    public String readOptionalLine(String prompt, String current) {
        String s = readLine(prompt + " [" + current + "]: ").trim();
        return s.isEmpty() ? current : s;
    }

    public double readOptionalDouble(String prompt, double current) {
        while (true) {
            String s = readLine(prompt + " [" + current + "]: ").trim();
            if (s.isEmpty()) return current;
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                showError("Please enter a number, or leave blank to keep the current value.");
            }
        }
    }

    // ---------- output helpers ----------
    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showError(String message) {
        System.out.println("[!] " + message);
    }

    public void showCandidate(Candidate c) {
        System.out.println(c != null ? c : "No access or candidate not found.");
    }

    public void showJobs(List<Job> jobs) {
        if (jobs.isEmpty()) {
            System.out.println("No jobs posted yet.");
            return;
        }
        for (Job j : jobs) System.out.println(j);
    }

    public void showMatchHeader(String candidateName, String strategyName) {
        System.out.println("Matching jobs for " + candidateName + " (strategy: " + strategyName + "):");
    }

    public void showMatch(String jobTitle, double percent) {
        System.out.printf("  %s - %.1f%% Match%n", jobTitle, percent);
    }

    public void showGoodbye() {
        System.out.println("Goodbye!");
    }
}
