package com.jobrec.mediator;

import com.jobrec.model.Application;

// MEDIATOR PATTERN — abstract Colleague.
// A department only knows the mediator, never the other departments.
public abstract class Department {
    protected final DepartmentMediator mediator;
    private final String name;

    protected Department(String name, DepartmentMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    public String getName() { return name; }

    // Called by the mediator when an application reaches this department
    public abstract void review(Application app);

    // Report the outcome back to the mediator
    protected void send(Application app, String event) {
        mediator.notify(this, app, event);
    }
}
