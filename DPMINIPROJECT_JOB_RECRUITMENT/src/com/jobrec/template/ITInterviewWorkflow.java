package com.jobrec.template;

import com.jobrec.model.Application;

// CONCRETE CLASS — IT interview: coding round + system-design discussion.
public class ITInterviewWorkflow extends InterviewWorkflow {

    @Override
    protected boolean conductInterview(Application app) {
        app.setCurrentStage("Coding Round");
        boolean coding = hasAnyRequiredSkill(app);
        System.out.println("[IT] Coding round on " + app.getJob().getRequiredSkills()
                + " -> " + (coding ? "Passed" : "Failed"));
        if (!coding) return false;

        app.setCurrentStage("System Design Round");
        System.out.println("[IT] System design discussion ("
                + app.getCandidate().getExperienceYears() + " yrs experience) -> Passed");
        return true;
    }

    @Override
    protected void makeOffer(Application app) {
        app.setCurrentStage("Offer");
        System.out.printf("[IT] Offer: %.0f per annum + laptop & remote-work allowance%n",
                app.getJob().getOfferedSalary());
        app.setStatus("Hired");
    }

    @Override
    protected String getWorkflowName() { return "IT Interview Workflow"; }
}
