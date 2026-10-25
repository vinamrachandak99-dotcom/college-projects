package com.jobrec.template;

import com.jobrec.model.Application;

// CONCRETE CLASS — Finance interview: aptitude test + case study.
public class FinanceInterviewWorkflow extends InterviewWorkflow {

    @Override
    protected boolean conductInterview(Application app) {
        app.setCurrentStage("Aptitude Test");
        String edu = app.getCandidate().getEducation().toLowerCase();
        boolean aptitude = edu.contains("b.") || edu.contains("m.");   // bachelor's / master's
        System.out.println("[Finance] Quantitative aptitude test -> " + (aptitude ? "Passed" : "Failed"));
        if (!aptitude) return false;

        app.setCurrentStage("Case Study");
        boolean caseStudy = hasAnyRequiredSkill(app) || app.getCandidate().getExperienceYears() >= 2;
        System.out.println("[Finance] Financial case study -> " + (caseStudy ? "Passed" : "Failed"));
        return caseStudy;
    }

    @Override
    protected void makeOffer(Application app) {
        app.setCurrentStage("Offer");
        double base = app.getJob().getOfferedSalary();
        System.out.printf("[Finance] Offer: %.0f base + %.0f annual performance bonus (10%%)%n",
                base, base * 0.10);
        app.setStatus("Hired");
    }

    @Override
    protected String getWorkflowName() { return "Finance Interview Workflow"; }
}
