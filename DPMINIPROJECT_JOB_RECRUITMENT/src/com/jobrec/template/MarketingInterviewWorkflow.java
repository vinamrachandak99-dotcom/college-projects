package com.jobrec.template;

import com.jobrec.model.Application;

// CONCRETE CLASS — Marketing interview: campaign pitch.
// (Added so every job category in the system has a workflow.)
public class MarketingInterviewWorkflow extends InterviewWorkflow {

    @Override
    protected boolean conductInterview(Application app) {
        app.setCurrentStage("Campaign Pitch");
        boolean pitch = hasAnyRequiredSkill(app);
        System.out.println("[Marketing] Campaign pitch presentation -> " + (pitch ? "Passed" : "Failed"));
        return pitch;
    }

    @Override
    protected void makeOffer(Application app) {
        app.setCurrentStage("Offer");
        System.out.printf("[Marketing] Offer: %.0f per annum + campaign incentive%n",
                app.getJob().getOfferedSalary());
        app.setStatus("Hired");
    }

    @Override
    protected String getWorkflowName() { return "Marketing Interview Workflow"; }
}
