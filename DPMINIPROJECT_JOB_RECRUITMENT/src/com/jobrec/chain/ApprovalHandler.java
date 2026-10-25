package com.jobrec.chain;

import com.jobrec.model.Application;

// CHAIN OF RESPONSIBILITY PATTERN
// Each concrete handler decides pass/fail for its own stage, then hands
// the application to the next handler in the chain (if any).
public abstract class ApprovalHandler {
    protected ApprovalHandler next;

    public ApprovalHandler setNext(ApprovalHandler next) {
        this.next = next;
        return next;   // allows chaining: h1.setNext(h2).setNext(h3)...
    }

    public final void process(Application app) {
        if (app.isRejected()) return;   // stop the chain early if already rejected
        boolean passed = handle(app);
        if (!passed) {
            app.markRejected(app.getCurrentStage());
            System.out.println(">> Rejected at stage: " + app.getCurrentStage());
            return;
        }
        if (next != null) {
            next.process(app);
        } else {
            app.setStatus("Hired");
            System.out.println(">> Application fully approved. Candidate hired!");
        }
    }

    // returns true if this stage passes, false if it should stop the chain here
    protected abstract boolean handle(Application app);
}
