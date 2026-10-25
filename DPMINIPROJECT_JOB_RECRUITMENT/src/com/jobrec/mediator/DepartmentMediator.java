package com.jobrec.mediator;

import com.jobrec.model.Application;

// MEDIATOR PATTERN — the Mediator interface.
// Departments never call each other directly; they only report events
// to the mediator, which decides what happens next.
public interface DepartmentMediator {

    String EVENT_PASSED = "PASSED";
    String EVENT_REJECTED = "REJECTED";

    void registerDepartment(Department department);

    void notify(Department sender, Application app, String event);
}
