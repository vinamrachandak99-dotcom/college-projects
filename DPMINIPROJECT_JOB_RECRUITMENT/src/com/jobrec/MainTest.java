package com.jobrec;

import com.jobrec.abstractfactory.*;
import com.jobrec.factory.*;
import com.jobrec.model.*;
import com.jobrec.chain.*;
import com.jobrec.mediator.RecruitmentCoordinator;
import com.jobrec.memento.ProfileHistory;
import com.jobrec.service.JobMatcher;
import com.jobrec.strategy.ExperienceHeavyStrategy;
import com.jobrec.strategy.SkillHeavyStrategy;
import com.jobrec.template.FinanceInterviewWorkflow;
import com.jobrec.template.ITInterviewWorkflow;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== Starting Automated Test Suite ===");
        try {
            testITEligibility();
            testFinanceEligibility();
            testApprovalChainSuccess();
            testApprovalChainSalaryFail();
            testBuilder();
            testPrototype();
            testStrategySwap();
            testMementoUndo();
            testTemplateMethod();
            testMediator();
            System.out.println("\n=== ALL TESTS PASSED SUCCESSFULLY! ===");
        } catch (Exception e) {
            System.err.println("\n[FAIL] Test execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testITEligibility() {
        ITRecruitmentFactory factory = new ITRecruitmentFactory();
        EligibilityCriteria criteria = factory.createEligibilityCriteria();

        Candidate eligible = new Candidate(1, "Alice", 25, "B.Tech", "Java, SQL", "2 years", "NY", 80000);
        Candidate ineligible = new Candidate(2, "Bob", 22, "Design", "Photoshop", "Fresher", "NY", 60000);

        if (!criteria.isEligible(eligible)) throw new RuntimeException("IT eligibility failed: Eligible candidate rejected.");
        if (criteria.isEligible(ineligible)) throw new RuntimeException("IT eligibility failed: Ineligible candidate accepted.");
        System.out.println("[PASS] IT Eligibility check.");
    }

    private static void testFinanceEligibility() {
        FinanceRecruitmentFactory factory = new FinanceRecruitmentFactory();
        EligibilityCriteria criteria = factory.createEligibilityCriteria();

        Candidate eligible = new Candidate(3, "Charlie", 28, "B.Com", "Excel", "3 years", "London", 90000);
        Candidate ineligible = new Candidate(4, "David", 22, "High School Diploma", "Excel", "Fresher", "London", 50000);

        if (!criteria.isEligible(eligible)) throw new RuntimeException("Finance eligibility failed: Eligible candidate rejected.");
        if (criteria.isEligible(ineligible)) throw new RuntimeException("Finance eligibility failed: Ineligible candidate accepted.");
        System.out.println("[PASS] Finance Eligibility check.");
    }

    private static void testApprovalChainSuccess() {
        Job job = new ITJob(1, "Java Developer", "Java, SQL", "NY", 100000);
        Candidate candidate = new Candidate(5, "Alice", 25, "B.Tech", "Java, SQL", "2 years", "NY", 95000);
        Application app = new Application(1001, candidate, job);

        ApprovalHandler hr = new HRScreeningHandler();
        ApprovalHandler tech = new TechnicalInterviewHandler();
        ApprovalHandler manager = new ManagerInterviewHandler();
        ApprovalHandler finalStage = new FinalApprovalHandler();
        hr.setNext(tech).setNext(manager).setNext(finalStage);

        app.setStatus("Pending");
        hr.process(app);

        if (!"Hired".equals(app.getStatus())) {
            throw new RuntimeException("Approval chain success test failed: Status is " + app.getStatus());
        }
        System.out.println("[PASS] Approval chain full approval flow.");
    }

    private static void testApprovalChainSalaryFail() {
        Job job = new ITJob(2, "C++ Dev", "C++", "NY", 100000);
        Candidate candidate = new Candidate(6, "Bob", 30, "M.Tech", "C++", "5 years", "NY", 130000); // 130k expected > 120k max allowed (1.2 * 100k)
        Application app = new Application(1002, candidate, job);

        ApprovalHandler hr = new HRScreeningHandler();
        ApprovalHandler tech = new TechnicalInterviewHandler();
        ApprovalHandler manager = new ManagerInterviewHandler();
        ApprovalHandler finalStage = new FinalApprovalHandler();
        hr.setNext(tech).setNext(manager).setNext(finalStage);

        app.setStatus("Pending");
        hr.process(app);

        if (!app.isRejected() || !"Rejected at Manager Interview".equals(app.getStatus())) {
            throw new RuntimeException("Approval chain salary fail test failed: Stage=" + app.getCurrentStage() + ", Status=" + app.getStatus());
        }
        System.out.println("[PASS] Approval chain salary ceiling validation (Manager Interview fails).");
    }

    // ---------- Phase 2 patterns ----------

    private static void check(boolean condition, String failMessage) {
        if (!condition) throw new RuntimeException(failMessage);
    }

    private static void testBuilder() {
        Candidate c = new Candidate.CandidateBuilder("Radha", 22)
                .setSkills("Java,SQL").setExpectedSalary(400000).build();
        check("Radha".equals(c.getName()) && c.getAge() == 22, "Builder: required fields not set");
        check("Java,SQL".equals(c.getSkills()), "Builder: optional skills not set");
        check("Fresher".equals(c.getExperience()), "Builder: default experience should be Fresher");
        boolean threw = false;
        try { new Candidate.CandidateBuilder("", 22).build(); } catch (IllegalStateException e) { threw = true; }
        check(threw, "Builder: blank name should be rejected");
        System.out.println("[PASS] Builder creates Candidate with defaults + validation.");
    }

    private static void testPrototype() {
        Job original = new ITJob(7, "Java Developer", "Java", "Pune", 500000);
        Job copy = original.clone();
        copy.setTitle("Senior Java Developer");
        copy.setOfferedSalary(800000);
        check(copy != original, "Prototype: clone returned same instance");
        check(copy instanceof ITJob, "Prototype: clone lost concrete type");
        check("Java Developer".equals(original.getTitle()) && original.getOfferedSalary() == 500000,
                "Prototype: editing clone changed the original");
        System.out.println("[PASS] Prototype clone is independent and keeps its type.");
    }

    private static void testStrategySwap() {
        Job job = new ITJob(8, "Dev", "Java, SQL", "NY", 100000);
        Candidate fresher = new Candidate(9, "F", 21, "B.Tech", "Java, SQL", "Fresher", "NY", 90000);
        JobMatcher matcher = new JobMatcher();
        double skillHeavy = matcher.matchPercentage(fresher, job);
        matcher.setStrategy(new ExperienceHeavyStrategy());
        double expHeavy = matcher.matchPercentage(fresher, job);
        check(skillHeavy == 100.0, "Strategy: skill-heavy should be 100% but was " + skillHeavy);
        check(expHeavy == 50.0, "Strategy: experience-heavy should be 50% for a fresher but was " + expHeavy);
        matcher.setStrategy(new SkillHeavyStrategy());
        check(matcher.matchPercentage(fresher, job) == skillHeavy, "Strategy: swapping back changed result");
        System.out.println("[PASS] Strategy swap at runtime changes the match score.");
    }

    private static void testMementoUndo() {
        Candidate c = new Candidate(10, "M", 25, "B.Tech", "Java", "1 year", "NY", 50000);
        ProfileHistory history = new ProfileHistory();
        history.save(c.saveToMemento());
        c.setSkills("Java,Python");
        history.save(c.saveToMemento());
        c.setExperience("3 years");

        c.restoreFromMemento(history.undo());
        check("Java,Python".equals(c.getSkills()) && "1 year".equals(c.getExperience()), "Memento: first undo wrong");
        c.restoreFromMemento(history.undo());
        check("Java".equals(c.getSkills()), "Memento: second undo wrong");
        check(history.isEmpty() && history.undo() == null, "Memento: history should be empty");
        System.out.println("[PASS] Memento save/undo restores previous profile states.");
    }

    private static void testTemplateMethod() {
        Candidate dev = new Candidate(11, "T", 25, "B.Tech", "Java", "2 years", "NY", 90000);
        Application itApp = new Application(2001, dev, new ITJob(12, "Dev", "Java", "NY", 100000));
        check(new ITInterviewWorkflow().processApplication(itApp) && "Hired".equals(itApp.getStatus()),
                "Template: IT workflow should hire, status=" + itApp.getStatus());

        Candidate noDegree = new Candidate(13, "N", 25, "High School", "Excel", "Fresher", "NY", 50000);
        Application finApp = new Application(2002, noDegree, new FinanceJob(14, "Analyst", "Excel", "NY", 60000));
        check(!new FinanceInterviewWorkflow().processApplication(finApp)
                        && "Rejected at Aptitude Test".equals(finApp.getStatus()),
                "Template: Finance workflow should reject at aptitude, status=" + finApp.getStatus());
        System.out.println("[PASS] Template Method runs fixed steps with category-specific hooks.");
    }

    private static void testMediator() {
        Candidate good = new Candidate(15, "G", 25, "B.Tech", "Java", "2 years", "NY", 100000);
        Application ok = new Application(3001, good, new ITJob(16, "Dev", "Java", "NY", 100000));
        RecruitmentCoordinator.withDefaultDepartments().startProcess(ok);
        check("Hired".equals(ok.getStatus()), "Mediator: should hire, status=" + ok.getStatus());

        Candidate pricey = new Candidate(17, "P", 25, "B.Tech", "Java", "2 years", "NY", 200000);
        Application bad = new Application(3002, pricey, new ITJob(18, "Dev", "Java", "NY", 100000));
        RecruitmentCoordinator.withDefaultDepartments().startProcess(bad);
        check("Rejected at Management Department".equals(bad.getStatus()),
                "Mediator: should reject at management, status=" + bad.getStatus());
        System.out.println("[PASS] Mediator routes HR -> Tech -> Management through the coordinator.");
    }
}
