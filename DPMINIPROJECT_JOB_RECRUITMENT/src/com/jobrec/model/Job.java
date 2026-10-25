package com.jobrec.model;

// Base "product" for the Factory Method pattern.
// Concrete job types (ITJob, FinanceJob, MarketingJob) extend this.
public abstract class Job {
    private int id;
    private String title;
    private String requiredSkills;  // comma separated
    private String location;
    private double offeredSalary;
    private String category;        // IT, Finance, Marketing...

    public Job(int id, String title, String requiredSkills, String location,
               double offeredSalary, String category) {
        this.id = id;
        this.title = title;
        this.requiredSkills = requiredSkills;
        this.location = location;
        this.offeredSalary = offeredSalary;
        this.category = category;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getLocation() { return location; }
    public double getOfferedSalary() { return offeredSalary; }
    public String getCategory() { return category; }

    // Every concrete job type can describe itself differently if needed
    public abstract String describe();

    @Override
    public String toString() {
        return "Job#" + id + " [" + title + " (" + category + "), skills=" + requiredSkills +
                ", loc=" + location + ", salary=" + offeredSalary + "]";
    }
}
