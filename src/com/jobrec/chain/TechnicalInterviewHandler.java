package com.jobrec.chain;

import com.jobrec.model.Application;

public class TechnicalInterviewHandler extends ApprovalHandler {
    @Override
    protected boolean handle(Application app) {
        app.setCurrentStage("Technical Interview");
        System.out.println("[Technical Interview] Evaluating skills for " +
                app.getCandidate().getName() + "...");
        // simple rule: candidate skills must overlap with job's required skills
        String[] candidateSkills = app.getCandidate().getSkills().toLowerCase().split(",");
        String requiredSkills = app.getJob().getRequiredSkills().toLowerCase();
        boolean pass = false;
        for (String skill : candidateSkills) {
            if (requiredSkills.contains(skill.trim())) {
                pass = true;
                break;
            }
        }
        System.out.println("[Technical Interview] " + (pass ? "Passed" : "Failed"));
        return pass;
    }
}
