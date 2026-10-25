package com.jobrec.mediator;

import com.jobrec.model.Application;

// CONCRETE COLLEAGUE — Management: budget sign-off (salary within 120%).
public class ManagementDepartment extends Department {

    public ManagementDepartment(DepartmentMediator mediator) {
        super("Management Department", mediator);
    }

    @Override
    public void review(Application app) {
        app.setCurrentStage(getName());
        boolean pass = app.getCandidate().getExpectedSalary() <= app.getJob().getOfferedSalary() * 1.2;
        System.out.println("[Management] Budget approval -> " + (pass ? "Approved" : "Rejected (salary mismatch)"));
        send(app, pass ? DepartmentMediator.EVENT_PASSED : DepartmentMediator.EVENT_REJECTED);
    }
}
