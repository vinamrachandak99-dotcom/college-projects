package com.jobrec.template;

import com.jobrec.model.Application;
import com.jobrec.model.Candidate;

// TEMPLATE METHOD PATTERN — the abstract class.
// processApplication() is the "template method": it is final, so every
// category runs the same fixed sequence of steps:
//     verifyDocuments()  ->  conductInterview()  ->  makeOffer()
// Subclasses only decide HOW the interview and offer are done.
public abstract class InterviewWorkflow {

    public final boolean processApplication(Application app) {
        System.out.println("\n=== " + getWorkflowName() + " for " + app.getCandidate().getName() + " ===");

        if (!verifyDocuments(app)) {
            app.markRejected("Document Verification");
            return false;
        }
        if (!conductInterview(app)) {
            app.markRejected(app.getCurrentStage());
            return false;
        }
        makeOffer(app);
        return true;
    }

    // Common step, shared by every category (can still be overridden if needed)
    protected boolean verifyDocuments(Application app) {
        app.setCurrentStage("Document Verification");
        Candidate c = app.getCandidate();
        boolean ok = c.getAge() >= 18
                && c.getEducation() != null && !c.getEducation().trim().isEmpty();
        System.out.println("[Documents] ID proof & education certificate "
                + (ok ? "verified" : "missing/invalid"));
        return ok;
    }

    // ---------- hooks that each category must implement ----------
    protected abstract boolean conductInterview(Application app);
    protected abstract void makeOffer(Application app);
    protected abstract String getWorkflowName();

    // Small helper for subclasses: does the candidate have any required skill?
    protected boolean hasAnyRequiredSkill(Application app) {
        String required = app.getJob().getRequiredSkills().toLowerCase();
        for (String skill : app.getCandidate().getSkills().toLowerCase().split(",")) {
            String s = skill.trim();
            if (!s.isEmpty() && required.contains(s)) return true;
        }
        return false;
    }
}
