package com.jobrec.chain;

import com.jobrec.model.Application;

public class FinalApprovalHandler extends ApprovalHandler {
    @Override
    protected boolean handle(Application app) {
        app.setCurrentStage("Final Approval");
        System.out.println("[Final Approval] Confirming offer for " +
                app.getCandidate().getName() + "...");
        return true;   // if it reached here, everything else already passed
    }
}
