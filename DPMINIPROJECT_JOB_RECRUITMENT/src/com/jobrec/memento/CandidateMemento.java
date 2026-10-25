package com.jobrec.memento;

// MEMENTO PATTERN — the Memento.
// An immutable snapshot of a Candidate's editable profile fields.
// Only the Originator (Candidate) creates and reads it; the Caretaker
// (ProfileHistory) just stores it without looking inside.
public final class CandidateMemento {
    private final String education;
    private final String skills;
    private final String experience;
    private final String location;
    private final double expectedSalary;

    public CandidateMemento(String education, String skills, String experience,
                            String location, double expectedSalary) {
        this.education = education;
        this.skills = skills;
        this.experience = experience;
        this.location = location;
        this.expectedSalary = expectedSalary;
    }

    public String getEducation() { return education; }
    public String getSkills() { return skills; }
    public String getExperience() { return experience; }
    public String getLocation() { return location; }
    public double getExpectedSalary() { return expectedSalary; }
}
