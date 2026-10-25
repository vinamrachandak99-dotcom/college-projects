package com.jobrec.controller;

import com.jobrec.abstractfactory.*;
import com.jobrec.bridge.EmailNotification;
import com.jobrec.bridge.NotificationMethod;
import com.jobrec.bridge.SMSNotification;
import com.jobrec.chain.*;
import com.jobrec.db.DBConnection;
import com.jobrec.mediator.RecruitmentCoordinator;
import com.jobrec.memento.ProfileHistory;
import com.jobrec.model.Application;
import com.jobrec.model.Candidate;
import com.jobrec.model.Job;
import com.jobrec.observer.CandidateObserver;
import com.jobrec.proxy.CandidateDataProxy;
import com.jobrec.service.JobMatcher;
import com.jobrec.strategy.ExperienceHeavyStrategy;
import com.jobrec.strategy.SkillHeavyStrategy;
import com.jobrec.template.FinanceInterviewWorkflow;
import com.jobrec.template.ITInterviewWorkflow;
import com.jobrec.template.InterviewWorkflow;
import com.jobrec.template.MarketingInterviewWorkflow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// MVC — the CONTROLLER.
// Serves as the API for the GUI view (SwingView) and coordinates interactions
// between the Model entities, Database (Singleton), and the 14 design patterns.
public class JobRecruitmentController {

    private final Connection conn;
    private final JobMatcher matcher = new JobMatcher(); // Strategy context
    private int appCounter = 1000;

    public JobRecruitmentController() {
        this.conn = DBConnection.getInstance().getConnection(); // Singleton
    }

    // =====================================================================
    // 1. Register Candidate (Builder Pattern)
    // =====================================================================
    public String registerCandidate(String name, int age, String education,
                                    String skills, String experience,
                                    String location, double expectedSalary) {
        try {
            Candidate candidate = new Candidate.CandidateBuilder(name, age)
                    .setEducation(education)
                    .setSkills(skills)
                    .setExperience(experience)
                    .setLocation(location)
                    .setExpectedSalary(expectedSalary)
                    .build();

            int id = insertCandidate(candidate);
            return "[SUCCESS] Candidate Registered via Builder Pattern!\n" +
                   "Candidate ID: " + id + "\n" +
                   "Name: " + candidate.getName() + " (Age: " + candidate.getAge() + ")\n" +
                   "Education: " + candidate.getEducation() + "\n" +
                   "Skills: " + candidate.getSkills() + "\n" +
                   "Experience: " + candidate.getExperience() + "\n" +
                   "Location: " + candidate.getLocation() + "\n" +
                   "Expected Salary: $" + String.format("%,.2f", candidate.getExpectedSalary());
        } catch (Exception e) {
            return "[ERROR] Failed to register candidate: " + e.getMessage();
        }
    }

    // =====================================================================
    // 2. Post Job (Abstract Factory Pattern)
    // =====================================================================
    public String postJob(String category, String title, String skills,
                          String location, double salary) {
        try {
            RecruitmentAbstractFactory factory = getFactoryFor(category);
            Job job = factory.createJob(0, title, skills, location, salary);

            int id = insertJob(job);
            job.setId(id);
            return "[SUCCESS] Job Posted via Abstract Factory (" + category.toUpperCase() + ")\n" +
                   "Job ID: " + id + "\n" +
                   "Description: " + job.describe() + "\n" +
                   "Location: " + job.getLocation() + " | Offered Salary: $" + String.format("%,.2f", job.getOfferedSalary());
        } catch (Exception e) {
            return "[ERROR] Failed to post job: " + e.getMessage();
        }
    }

    // =====================================================================
    // 3. View Candidate (Proxy Pattern: Role-based access control)
    // =====================================================================
    public String getCandidateDetails(String role, int candidateId) {
        try {
            CandidateDataProxy proxy = new CandidateDataProxy(role.trim().toUpperCase());
            Candidate candidate = proxy.getCandidateDetails(candidateId);
            if (candidate == null) {
                return "[ACCESS DENIED / NOT FOUND]\n" +
                       "Role '" + role + "' does not have permission to view Candidate #" + candidateId +
                       " or the candidate does not exist.\n(Only RECRUITER and CANDIDATE roles are authorized by Proxy)";
            }
            return "[ACCESS GRANTED via Proxy] Role: " + role.toUpperCase() + "\n" +
                   "Candidate Details:\n" + candidate;
        } catch (Exception e) {
            return "[ERROR] Error viewing candidate: " + e.getMessage();
        }
    }

    // =====================================================================
    // 4. View Jobs
    // =====================================================================
    public List<Job> getAvailableJobs() {
        try {
            return loadAllJobs();
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    public String getAllJobsFormatted() {
        try {
            List<Job> jobs = loadAllJobs();
            if (jobs.isEmpty()) {
                return "No jobs posted in the database yet.\nUse 'Post Job' to add one.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("=== Available Job Postings (").append(jobs.size()).append(" Found) ===\n\n");
            for (Job j : jobs) {
                sb.append(String.format("Job #%-3d | %-24s | Category: %-9s\n", j.getId(), j.getTitle(), j.getCategory()));
                sb.append("  Skills  : ").append(j.getRequiredSkills()).append("\n");
                sb.append("  Location: ").append(j.getLocation());
                sb.append(String.format(" | Offered Salary: $%,.2f\n", j.getOfferedSalary()));
                sb.append("----------------------------------------------------------------------\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "[ERROR] Failed to load jobs: " + e.getMessage();
        }
    }

    // =====================================================================
    // 5. Search Matching Jobs (Strategy Pattern)
    // =====================================================================
    public String searchJobs(int candidateId, String strategyType) {
        try {
            Candidate candidate = loadCandidate(candidateId);
            if (candidate == null) {
                return "[ERROR] Candidate #" + candidateId + " not found.";
            }

            if (strategyType != null && strategyType.toLowerCase().contains("experience")) {
                matcher.setStrategy(new ExperienceHeavyStrategy());
            } else {
                matcher.setStrategy(new SkillHeavyStrategy());
            }

            List<Job> jobs = loadAllJobs();
            if (jobs.isEmpty()) {
                return "No jobs available for matching.";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("=== Matching Jobs for Candidate: ").append(candidate.getName());
            sb.append(" (ID: ").append(candidate.getId()).append(") ===\n");
            sb.append("Candidate Skills: ").append(candidate.getSkills());
            sb.append(" | Experience: ").append(candidate.getExperience()).append("\n");
            sb.append("Active Strategy : ").append(matcher.getStrategy().getName()).append("\n\n");

            for (Job j : jobs) {
                double pct = matcher.matchPercentage(candidate, j);
                sb.append(String.format("  • %-26s [%-8s] => %5.1f%% Match  (Req: %s)\n",
                        j.getTitle(), j.getCategory(), pct, j.getRequiredSkills()));
            }
            return sb.toString();
        } catch (Exception e) {
            return "[ERROR] Job matching failed: " + e.getMessage();
        }
    }

    // =====================================================================
    // 6. Apply for Job (Abstract Factory + Chain + Observer + Bridge)
    // =====================================================================
    public String applyForJob(int candidateId, int jobId, String notifyChannel, String contact) {
        try {
            Candidate candidate = loadCandidate(candidateId);
            Job job = loadJob(jobId);
            if (candidate == null) return "[ERROR] Candidate #" + candidateId + " not found.";
            if (job == null) return "[ERROR] Job #" + jobId + " not found.";

            // 1. Abstract Factory Eligibility check
            RecruitmentAbstractFactory factory = getFactoryFor(job.getCategory());
            EligibilityCriteria criteria = factory.createEligibilityCriteria();
            if (!criteria.isEligible(candidate)) {
                return "[APPLICATION REJECTED at Eligibility Check]\n" +
                       "Candidate does not meet category criteria for " + job.getCategory() + ".\n" +
                       "Application has been halted prior to interview stages.";
            }

            Application application = new Application(appCounter++, candidate, job);

            // 2. Observer + Bridge: Notification setup
            NotificationMethod method = (notifyChannel != null && notifyChannel.equalsIgnoreCase("SMS"))
                    ? new SMSNotification() : new EmailNotification();
            application.subscribe(new CandidateObserver(candidate, contact, method));

            // 3. Chain of Responsibility: Approval Handlers
            ApprovalHandler hr = new HRScreeningHandler();
            ApprovalHandler tech = new TechnicalInterviewHandler();
            ApprovalHandler manager = new ManagerInterviewHandler();
            ApprovalHandler finalStage = new FinalApprovalHandler();
            hr.setNext(tech).setNext(manager).setNext(finalStage);

            // Capture all pattern prints
            String logs = captureOutput(() -> {
                application.setStatus("Pending"); // Triggers initial notification
                hr.process(application);
            });

            saveApplication(candidateId, jobId, application);

            return "=== Application Process Trace ===\n" +
                   "Candidate : " + candidate.getName() + " (ID: " + candidate.getId() + ")\n" +
                   "Job       : " + job.getTitle() + " (ID: " + job.getId() + ", " + job.getCategory() + ")\n" +
                   "Channel   : " + notifyChannel + " (" + contact + ")\n" +
                   "----------------------------------------------------\n" +
                   logs +
                   "----------------------------------------------------\n" +
                   "Final Application Status: " + application.getStatus() + "\n" +
                   "Saved to SQLite applications table successfully.";
        } catch (Exception e) {
            return "[ERROR] Application process failed: " + e.getMessage();
        }
    }

    // =====================================================================
    // 7. Duplicate Job Posting (Prototype Pattern)
    // =====================================================================
    public String duplicateJob(int jobId, String newTitle, Double newSalary, String newLocation) {
        try {
            Job original = loadJob(jobId);
            if (original == null) {
                return "[ERROR] Original Job #" + jobId + " not found.";
            }

            // Prototype: clone creates an exact copy of the concrete subclass
            Job copy = original.clone();
            copy.setId(0);
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                copy.setTitle(newTitle.trim());
            }
            if (newSalary != null && newSalary > 0) {
                copy.setOfferedSalary(newSalary);
            }
            if (newLocation != null && !newLocation.trim().isEmpty()) {
                copy.setLocation(newLocation.trim());
            }

            int newId = insertJob(copy);
            copy.setId(newId);

            return "[SUCCESS] Job Duplicated via Prototype Pattern!\n\n" +
                   "ORIGINAL JOB:\n" + original + "\n\n" +
                   "CLONED DUPLICATE JOB:\n" + copy + "\n" +
                   "Category preserved: " + copy.getCategory() + " (" + copy.getClass().getSimpleName() + ")";
        } catch (Exception e) {
            return "[ERROR] Failed to duplicate job: " + e.getMessage();
        }
    }

    // =====================================================================
    // 8. Edit Candidate Profile (Memento Pattern)
    // =====================================================================
    public Candidate getCandidate(int candidateId) {
        try {
            return loadCandidate(candidateId);
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean updateCandidateProfile(Candidate candidate) {
        try {
            updateCandidate(candidate);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // =====================================================================
    // 9. Run Interview Workflow (Template Method Pattern)
    // =====================================================================
    public String runInterviewWorkflow(int candidateId, int jobId) {
        try {
            Candidate candidate = loadCandidate(candidateId);
            Job job = loadJob(jobId);
            if (candidate == null) return "[ERROR] Candidate #" + candidateId + " not found.";
            if (job == null) return "[ERROR] Job #" + jobId + " not found.";

            Application application = new Application(appCounter++, candidate, job);
            InterviewWorkflow workflow = getWorkflowFor(job.getCategory());

            String logs = captureOutput(() -> {
                workflow.processApplication(application);
            });

            saveApplication(candidateId, jobId, application);

            return "=== Template Method: " + workflow.getClass().getSimpleName() + " ===\n" +
                   logs + "\n" +
                   "Final Outcome: " + application.getStatus() + " (Stage: " + application.getCurrentStage() + ")";
        } catch (Exception e) {
            return "[ERROR] Interview workflow failed: " + e.getMessage();
        }
    }

    // =====================================================================
    // 10. Department Coordination (Mediator Pattern)
    // =====================================================================
    public String processViaDepartments(int candidateId, int jobId) {
        try {
            Candidate candidate = loadCandidate(candidateId);
            Job job = loadJob(jobId);
            if (candidate == null) return "[ERROR] Candidate #" + candidateId + " not found.";
            if (job == null) return "[ERROR] Job #" + jobId + " not found.";

            Application application = new Application(appCounter++, candidate, job);
            RecruitmentCoordinator coordinator = RecruitmentCoordinator.withDefaultDepartments();

            String logs = captureOutput(() -> {
                coordinator.startProcess(application);
            });

            saveApplication(candidateId, jobId, application);

            return "=== Mediator Pattern: Inter-Department Routing ===\n" +
                   logs + "\n" +
                   "Final Status: " + application.getStatus() + " (Current Stage: " + application.getCurrentStage() + ")";
        } catch (Exception e) {
            return "[ERROR] Department coordination failed: " + e.getMessage();
        }
    }

    // =====================================================================
    // Helper: Category Factory / Workflow Mapping
    // =====================================================================
    private RecruitmentAbstractFactory getFactoryFor(String category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        switch (category.toLowerCase().trim()) {
            case "it": return new ITRecruitmentFactory();
            case "finance": return new FinanceRecruitmentFactory();
            case "marketing": return new MarketingRecruitmentFactory();
            default: throw new IllegalArgumentException("Unknown category: " + category);
        }
    }

    private InterviewWorkflow getWorkflowFor(String category) {
        if (category == null) throw new IllegalArgumentException("Category cannot be null");
        switch (category.toLowerCase().trim()) {
            case "it": return new ITInterviewWorkflow();
            case "finance": return new FinanceInterviewWorkflow();
            case "marketing": return new MarketingInterviewWorkflow();
            default: throw new IllegalArgumentException("No interview workflow for category: " + category);
        }
    }

    // =====================================================================
    // Helper: System.out Capture for UI Log Area
    // =====================================================================
    @FunctionalInterface
    private interface ThrowableAction {
        void run() throws Exception;
    }

    private synchronized String captureOutput(ThrowableAction action) throws Exception {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream tee = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                originalOut.write(b);
                baos.write(b);
            }
            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                originalOut.write(b, off, len);
                baos.write(b, off, len);
            }
            @Override
            public void flush() throws IOException {
                originalOut.flush();
                baos.flush();
            }
        };
        PrintStream teePrint = new PrintStream(tee, true, StandardCharsets.UTF_8.name());
        try {
            System.setOut(teePrint);
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }

    // =====================================================================
    // Helper: SQLite Database Operations
    // =====================================================================
    private int insertCandidate(Candidate c) throws SQLException {
        String sql = "INSERT INTO candidates(name, age, education, skills, experience, location, expected_salary) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setInt(2, c.getAge());
            ps.setString(3, c.getEducation());
            ps.setString(4, c.getSkills());
            ps.setString(5, c.getExperience());
            ps.setString(6, c.getLocation());
            ps.setDouble(7, c.getExpectedSalary());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    private void updateCandidate(Candidate c) throws SQLException {
        String sql = "UPDATE candidates SET education = ?, skills = ?, experience = ?, location = ?, expected_salary = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getEducation());
            ps.setString(2, c.getSkills());
            ps.setString(3, c.getExperience());
            ps.setString(4, c.getLocation());
            ps.setDouble(5, c.getExpectedSalary());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
        }
    }

    private int insertJob(Job job) throws SQLException {
        String sql = "INSERT INTO jobs(title, required_skills, location, offered_salary, category) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getRequiredSkills());
            ps.setString(3, job.getLocation());
            ps.setDouble(4, job.getOfferedSalary());
            ps.setString(5, job.getCategory());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    private void saveApplication(int candidateId, int jobId, Application app) throws SQLException {
        String sql = "INSERT INTO applications(candidate_id, job_id, stage, status) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, candidateId);
            ps.setInt(2, jobId);
            ps.setString(3, app.getCurrentStage());
            ps.setString(4, app.getStatus());
            ps.executeUpdate();
        }
    }

    private Candidate loadCandidate(int id) throws SQLException {
        String sql = "SELECT * FROM candidates WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Candidate.CandidateBuilder(rs.getString("name"), rs.getInt("age"))
                        .setId(rs.getInt("id"))
                        .setEducation(rs.getString("education"))
                        .setSkills(rs.getString("skills"))
                        .setExperience(rs.getString("experience"))
                        .setLocation(rs.getString("location"))
                        .setExpectedSalary(rs.getDouble("expected_salary"))
                        .build();
            }
        }
        return null;
    }

    private Job loadJob(int id) throws SQLException {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return jobFromRow(rs);
        }
        return null;
    }

    private List<Job> loadAllJobs() throws SQLException {
        List<Job> jobs = new ArrayList<>();
        String sql = "SELECT * FROM jobs";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) jobs.add(jobFromRow(rs));
        }
        return jobs;
    }

    private Job jobFromRow(ResultSet rs) throws SQLException {
        RecruitmentAbstractFactory factory = getFactoryFor(rs.getString("category"));
        return factory.createJob(rs.getInt("id"), rs.getString("title"),
                rs.getString("required_skills"), rs.getString("location"),
                rs.getDouble("offered_salary"));
    }
}
