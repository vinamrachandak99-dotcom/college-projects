package com.jobrec.chain;

import com.jobrec.model.Application;

public class ManagerInterviewHandler extends ApprovalHandler {
    @Override
    protected boolean handle(Application app) {
        app.setCurrentStage("Manager Interview");
        System.out.println("[Manager Interview] Culture-fit check for " +
                app.getCandidate().getName() + "...");
        // simple rule: expected salary must not exceed offered salary by more than 20%
        boolean pass = app.getCandidate().getExpectedSalary() <= app.getJob().getOfferedSalary() * 1.2;
        System.out.println("[Manager Interview] " + (pass ? "Passed" : "Failed (salary mismatch)"));
        return pass;
    }
}
