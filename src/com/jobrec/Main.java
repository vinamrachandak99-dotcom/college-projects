package com.jobrec;

import com.jobrec.abstractfactory.*;
import com.jobrec.bridge.EmailNotification;
import com.jobrec.bridge.NotificationMethod;
import com.jobrec.bridge.SMSNotification;
import com.jobrec.chain.*;
import com.jobrec.db.DBConnection;
import com.jobrec.model.Application;
import com.jobrec.model.Candidate;
import com.jobrec.model.Job;
import com.jobrec.observer.CandidateObserver;
import com.jobrec.proxy.CandidateDataProxy;
import com.jobrec.service.JobMatcher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static Scanner sc = new Scanner(System.in);
    private static Connection conn = DBConnection.getInstance().getConnection();
    private static int appCounter = 1000;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Job Search & Recruitment Management System ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": registerCandidate(); break;
                case "2": postJob(); break;
                case "3": viewCandidates(); break;
                case "4": viewJobs(); break;
                case "5": searchMatchingJobs(); break;
                case "6": applyForJob(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid choice, try again.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n1. Register Candidate");
        System.out.println("2. Post Job (Recruiter)");
        System.out.println("3. View Candidates");
        System.out.println("4. View Jobs");
        System.out.println("5. Search Matching Jobs for a Candidate");
        System.out.println("6. Apply for a Job (runs eligibility + approval chain + notifications)");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    // ---------- 1. Register Candidate ----------
    private static void registerCandidate() throws SQLException {
        System.out.print("Name: "); String name = sc.nextLine();
        System.out.print("Age: "); int age = Integer.parseInt(sc.nextLine());
        System.out.print("Education (e.g. B.Tech CSE): "); String education = sc.nextLine();
        System.out.print("Skills (comma separated): "); String skills = sc.nextLine();
        System.out.print("Experience (e.g. Fresher / 2 years): "); String experience = sc.nextLine();
        System.out.print("Preferred Location: "); String location = sc.nextLine();
        System.out.print("Expected Salary: "); double salary = Double.parseDouble(sc.nextLine());

        String sql = "INSERT INTO candidates(name, age, education, skills, experience, location, expected_salary) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setInt(2, age); ps.setString(3, education);
            ps.setString(4, skills); ps.setString(5, experience); ps.setString(6, location);
            ps.setDouble(7, salary);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            System.out.println("Registered! Candidate ID = " + id);
        }
    }

    // ---------- 2. Post Job (uses Abstract Factory for the whole "family") ----------
    private static void postJob() throws SQLException {
        System.out.print("Category (IT / Finance / Marketing): "); String category = sc.nextLine().trim();
        System.out.print("Job Title: "); String title = sc.nextLine();
        System.out.print("Required Skills (comma separated): "); String skills = sc.nextLine();
        System.out.print("Location: "); String location = sc.nextLine();
        System.out.print("Offered Salary: "); double salary = Double.parseDouble(sc.nextLine());

        RecruitmentAbstractFactory factory = getFactoryFor(category);
        Job job = factory.createJob(0, title, skills, location, salary);
        // (EligibilityCriteria + NotificationTemplate from this same factory get used later,
        //  e.g. in applyForJob(), guaranteeing they always match this job's category)

        String sql = "INSERT INTO jobs(title, required_skills, location, offered_salary, category) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, job.getTitle()); ps.setString(2, job.getRequiredSkills());
            ps.setString(3, job.getLocation()); ps.setDouble(4, job.getOfferedSalary());
            ps.setString(5, job.getCategory());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;
            System.out.println("Job posted! Job ID = " + id + " | " + job.describe());
        }
    }

    private static RecruitmentAbstractFactory getFactoryFor(String category) {
        switch (category.toLowerCase()) {
            case "it": return new ITRecruitmentFactory();
            case "finance": return new FinanceRecruitmentFactory();
            case "marketing": return new MarketingRecruitmentFactory();
            default: throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    // ---------- 3. View Candidates (via Proxy) ----------
    private static void viewCandidates() {
        System.out.print("Your role (RECRUITER / CANDIDATE / GUEST): ");
        String role = sc.nextLine().trim().toUpperCase();
        System.out.print("Candidate ID to view: ");
        int id = Integer.parseInt(sc.nextLine());

        CandidateDataProxy proxy = new CandidateDataProxy(role);
        Candidate c = proxy.getCandidateDetails(id);
        System.out.println(c != null ? c : "No access or candidate not found.");
    }

    // ---------- 4. View Jobs ----------
    private static void viewJobs() throws SQLException {
        List<Job> jobs = loadAllJobs();
        if (jobs.isEmpty()) { System.out.println("No jobs posted yet."); return; }
        for (Job j : jobs) System.out.println(j);
    }

    // ---------- 5. Search Matching Jobs ----------
    private static void searchMatchingJobs() throws SQLException {
        System.out.print("Candidate ID: ");
        int id = Integer.parseInt(sc.nextLine());
        Candidate candidate = loadCandidate(id);
        if (candidate == null) { System.out.println("Candidate not found."); return; }

        List<Job> jobs = loadAllJobs();
        System.out.println("Matching jobs for " + candidate.getName() + ":");
        for (Job j : jobs) {
            double pct = JobMatcher.matchPercentage(candidate, j);
            System.out.printf("  %s - %.1f%% Match%n", j.getTitle(), pct);
        }
    }

    // ---------- 6. Apply for a Job (the full pipeline) ----------
    private static void applyForJob() throws SQLException {
        System.out.print("Candidate ID: "); int candidateId = Integer.parseInt(sc.nextLine());
        System.out.print("Job ID: "); int jobId = Integer.parseInt(sc.nextLine());

        Candidate candidate = loadCandidate(candidateId);
        Job job = loadJob(jobId);
        if (candidate == null || job == null) { System.out.println("Invalid candidate or job ID."); return; }

        // Abstract Factory: run the category-specific eligibility check first
        RecruitmentAbstractFactory factory = getFactoryFor(job.getCategory());
        EligibilityCriteria criteria = factory.createEligibilityCriteria();
        if (!criteria.isEligible(candidate)) {
            System.out.println("Not eligible for this job category (" + job.getCategory() + "). Application stopped.");
            return;
        }
        System.out.println("Eligibility check passed for category: " + job.getCategory());

        Application application = new Application(appCounter++, candidate, job);

        // Observer: candidate subscribes to their own application, delivery via Bridge
        System.out.print("Notify by (EMAIL / SMS): ");
        String channel = sc.nextLine().trim().toUpperCase();
        NotificationMethod method = channel.equals("SMS") ? new SMSNotification() : new EmailNotification();
        System.out.print("Contact address (email or phone): ");
        String contact = sc.nextLine();
        application.subscribe(new CandidateObserver(candidate, contact, method));

        // Chain of Responsibility: build and run the approval pipeline
        ApprovalHandler hr = new HRScreeningHandler();
        ApprovalHandler tech = new TechnicalInterviewHandler();
        ApprovalHandler manager = new ManagerInterviewHandler();
        ApprovalHandler finalStage = new FinalApprovalHandler();
        hr.setNext(tech).setNext(manager).setNext(finalStage);

        application.setStatus("Pending");  // triggers first notification too
        hr.process(application);

        // persist the outcome
        String sql = "INSERT INTO applications(candidate_id, job_id, stage, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId); ps.setInt(2, jobId);
            ps.setString(3, application.getCurrentStage()); ps.setString(4, application.getStatus());
            ps.executeUpdate();
        }
        System.out.println("Final status: " + application.getStatus());
    }

    // ---------- helpers ----------
    private static Candidate loadCandidate(int id) throws SQLException {
        String sql = "SELECT * FROM candidates WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Candidate(rs.getInt("id"), rs.getString("name"), rs.getInt("age"),
                        rs.getString("education"), rs.getString("skills"), rs.getString("experience"),
                        rs.getString("location"), rs.getDouble("expected_salary"));
            }
        }
        return null;
    }

    private static Job loadJob(int id) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                RecruitmentAbstractFactory factory = getFactoryFor(rs.getString("category"));
                return factory.createJob(rs.getInt("id"), rs.getString("title"),
                        rs.getString("required_skills"), rs.getString("location"),
                        rs.getDouble("offered_salary"));
            }
        }
        return null;
    }

    private static List<Job> loadAllJobs() throws SQLException {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RecruitmentAbstractFactory factory = getFactoryFor(rs.getString("category"));
                jobs.add(factory.createJob(rs.getInt("id"), rs.getString("title"),
                        rs.getString("required_skills"), rs.getString("location"),
                        rs.getDouble("offered_salary")));
            }
        }
        return jobs;
    }
}
