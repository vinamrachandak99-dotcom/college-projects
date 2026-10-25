package com.jobrec.model;

public class Candidate {
    private int id;
    private String name;
    private int age;
    private String education;
    private String skills;       // comma separated, e.g. "Java,Python,SQL"
    private String experience;   // e.g. "Fresher" or "2 years"
    private String location;
    private double expectedSalary;

    public Candidate(int id, String name, int age, String education, String skills,
                      String experience, String location, double expectedSalary) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.education = education;
        this.skills = skills;
        this.experience = experience;
        this.location = location;
        this.expectedSalary = expectedSalary;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEducation() { return education; }
    public String getSkills() { return skills; }
    public String getExperience() { return experience; }
    public String getLocation() { return location; }
    public double getExpectedSalary() { return expectedSalary; }

    @Override
    public String toString() {
        return "Candidate#" + id + " [" + name + ", " + age + "yrs, " + education +
                ", skills=" + skills + ", exp=" + experience + ", loc=" + location +
                ", expSalary=" + expectedSalary + "]";
    }
}
