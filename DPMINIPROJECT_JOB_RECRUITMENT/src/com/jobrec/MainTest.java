package com.jobrec;

import com.jobrec.abstractfactory.*;
import com.jobrec.factory.*;
import com.jobrec.model.*;
import com.jobrec.chain.*;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("=== Starting Automated Test Suite ===");
        try {
            testITEligibility();
            testFinanceEligibility();
            testApprovalChainSuccess();
            testApprovalChainSalaryFail();
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
}
