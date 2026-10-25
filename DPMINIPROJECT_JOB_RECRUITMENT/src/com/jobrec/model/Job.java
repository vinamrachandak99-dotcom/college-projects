package com.jobrec.model;

// Base "product" for the Factory Method pattern.
// Concrete job types (ITJob, FinanceJob, MarketingJob) extend this.
//
// PROTOTYPE PATTERN
// Job implements Cloneable so a recruiter can duplicate an existing posting
// (keeping its concrete type — ITJob stays ITJob, etc.) and tweak it.
public abstract class Job implements Cloneable {
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

    // Setters used when customising a cloned job
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
    public void setLocation(String location) { this.location = location; }
    public void setOfferedSalary(double offeredSalary) { this.offeredSalary = offeredSalary; }

    // Every concrete job type can describe itself differently if needed
    public abstract String describe();

    // ---------- Prototype ----------
    // All fields are primitives or immutable Strings, so a shallow copy
    // is a safe, fully independent copy.
    @Override
    public Job clone() {
        try {
            return (Job) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Job is Cloneable, this cannot happen", e);
        }
    }

    @Override
    public String toString() {
        return "Job#" + id + " [" + title + " (" + category + "), skills=" + requiredSkills +
                ", loc=" + location + ", salary=" + offeredSalary + "]";
    }
}
