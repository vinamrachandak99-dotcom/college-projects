package com.jobrec.chain;

import com.jobrec.model.Application;

public class HRScreeningHandler extends ApprovalHandler {
    @Override
    protected boolean handle(Application app) {
        app.setCurrentStage("HR Screening");
        System.out.println("[HR Screening] Checking basic eligibility for " +
                app.getCandidate().getName() + "...");
        // simple rule: candidate must be at least 18
        boolean pass = app.getCandidate().getAge() >= 18;
        System.out.println("[HR Screening] " + (pass ? "Passed" : "Failed"));
        return pass;
    }
}
