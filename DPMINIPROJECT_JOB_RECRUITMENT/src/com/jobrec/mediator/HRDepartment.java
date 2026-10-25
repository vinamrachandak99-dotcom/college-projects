package com.jobrec.mediator;

import com.jobrec.model.Application;

// CONCRETE COLLEAGUE — HR: basic screening (age + education present).
public class HRDepartment extends Department {

    public HRDepartment(DepartmentMediator mediator) {
        super("HR Department", mediator);
    }

    @Override
    public void review(Application app) {
        app.setCurrentStage(getName());
        boolean pass = app.getCandidate().getAge() >= 18
                && !app.getCandidate().getEducation().trim().isEmpty();
        System.out.println("[HR] Screening " + app.getCandidate().getName() + " -> " + (pass ? "Passed" : "Failed"));
        send(app, pass ? DepartmentMediator.EVENT_PASSED : DepartmentMediator.EVENT_REJECTED);
    }
}
