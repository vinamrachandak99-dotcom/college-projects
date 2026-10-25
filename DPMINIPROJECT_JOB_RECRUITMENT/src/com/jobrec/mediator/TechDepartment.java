package com.jobrec.mediator;

import com.jobrec.model.Application;

// CONCRETE COLLEAGUE — Tech: checks skill overlap with the job.
public class TechDepartment extends Department {

    public TechDepartment(DepartmentMediator mediator) {
        super("Tech Department", mediator);
    }

    @Override
    public void review(Application app) {
        app.setCurrentStage(getName());
        String required = app.getJob().getRequiredSkills().toLowerCase();
        boolean pass = false;
        for (String skill : app.getCandidate().getSkills().toLowerCase().split(",")) {
            String s = skill.trim();
            if (!s.isEmpty() && required.contains(s)) { pass = true; break; }
        }
        System.out.println("[Tech] Skill assessment -> " + (pass ? "Passed" : "Failed"));
        send(app, pass ? DepartmentMediator.EVENT_PASSED : DepartmentMediator.EVENT_REJECTED);
    }
}
