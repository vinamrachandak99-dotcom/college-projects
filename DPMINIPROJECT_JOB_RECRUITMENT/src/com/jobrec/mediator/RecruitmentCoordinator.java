package com.jobrec.mediator;

import com.jobrec.model.Application;

// MEDIATOR PATTERN — the Concrete Mediator.
// Holds references to every department and owns the routing rules:
//   HR passed         -> forward to Tech
//   Tech passed       -> forward to Management
//   Management passed -> mark application Hired
//   anyone rejected   -> mark application Rejected at that department
// Changing the hiring flow only requires editing this class.
public class RecruitmentCoordinator implements DepartmentMediator {

    private HRDepartment hr;
    private TechDepartment tech;
    private ManagementDepartment management;

    // Convenience: creates and registers the three standard departments
    public static RecruitmentCoordinator withDefaultDepartments() {
        RecruitmentCoordinator c = new RecruitmentCoordinator();
        c.registerDepartment(new HRDepartment(c));
        c.registerDepartment(new TechDepartment(c));
        c.registerDepartment(new ManagementDepartment(c));
        return c;
    }

    @Override
    public void registerDepartment(Department department) {
        if (department instanceof HRDepartment) hr = (HRDepartment) department;
        else if (department instanceof TechDepartment) tech = (TechDepartment) department;
        else if (department instanceof ManagementDepartment) management = (ManagementDepartment) department;
        else throw new IllegalArgumentException("Unknown department: " + department.getName());
    }

    // Entry point: hand a new application to the first department
    public void startProcess(Application app) {
        if (hr == null || tech == null || management == null)
            throw new IllegalStateException("All departments must be registered first");
        System.out.println("[Coordinator] Routing application #" + app.getId() + " to " + hr.getName());
        hr.review(app);
    }

    @Override
    public void notify(Department sender, Application app, String event) {
        if (EVENT_REJECTED.equals(event)) {
            System.out.println("[Coordinator] " + sender.getName() + " rejected the application.");
            app.markRejected(sender.getName());
            return;
        }
        if (sender == hr) {
            System.out.println("[Coordinator] HR cleared -> forwarding to " + tech.getName());
            tech.review(app);
        } else if (sender == tech) {
            System.out.println("[Coordinator] Tech cleared -> forwarding to " + management.getName());
            management.review(app);
        } else if (sender == management) {
            System.out.println("[Coordinator] Management approved -> candidate hired.");
            app.setStatus("Hired");
        }
    }
}
