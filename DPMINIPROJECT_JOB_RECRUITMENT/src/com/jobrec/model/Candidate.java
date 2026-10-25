package com.jobrec.model;

import com.jobrec.memento.CandidateMemento;

// BUILDER PATTERN  -> see the static inner class CandidateBuilder below.
// MEMENTO PATTERN  -> Candidate is the "Originator": it can snapshot its
//                     editable profile into a CandidateMemento and restore it.
public class Candidate {
    private int id;
    private String name;
    private int age;
    private String education;
    private String skills;       // comma separated, e.g. "Java,Python,SQL"
    private String experience;   // e.g. "Fresher" or "2 years"
    private String location;
    private double expectedSalary;

    // Kept public for backward compatibility (proxy / tests use it directly).
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

    // Used by the Builder
    private Candidate(CandidateBuilder b) {
        this(b.id, b.name, b.age, b.education, b.skills, b.experience, b.location, b.expectedSalary);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getEducation() { return education; }
    public String getSkills() { return skills; }
    public String getExperience() { return experience; }
    public String getLocation() { return location; }
    public double getExpectedSalary() { return expectedSalary; }

    // Editable profile fields (needed for profile editing + Memento restore)
    public void setEducation(String education) { this.education = education; }
    public void setSkills(String skills) { this.skills = skills; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setLocation(String location) { this.location = location; }
    public void setExpectedSalary(double expectedSalary) { this.expectedSalary = expectedSalary; }

    // Parses "Fresher" -> 0, "2 years" -> 2, "1.5 yrs" -> 1 (whole years only)
    public int getExperienceYears() {
        if (experience == null) return 0;
        String digits = experience.replaceAll("[^0-9.]", " ").trim();
        if (digits.isEmpty()) return 0;
        try {
            return (int) Double.parseDouble(digits.split("\\s+")[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ---------- Memento: Originator methods ----------
    public CandidateMemento saveToMemento() {
        return new CandidateMemento(education, skills, experience, location, expectedSalary);
    }

    public void restoreFromMemento(CandidateMemento m) {
        if (m == null) return;
        this.education = m.getEducation();
        this.skills = m.getSkills();
        this.experience = m.getExperience();
        this.location = m.getLocation();
        this.expectedSalary = m.getExpectedSalary();
    }

    @Override
    public String toString() {
        return "Candidate#" + id + " [" + name + ", " + age + "yrs, " + education +
                ", skills=" + skills + ", exp=" + experience + ", loc=" + location +
                ", expSalary=" + expectedSalary + "]";
    }

    // =====================================================================
    // BUILDER PATTERN
    // name + age are required (constructor args); everything else is
    // optional with sensible defaults, set through chained setters.
    // Usage:
    //   new Candidate.CandidateBuilder("Radha", 22)
    //        .setSkills("Java,SQL").setExperience("Fresher").build();
    // =====================================================================
    public static class CandidateBuilder {
        private final String name;
        private final int age;
        private int id = 0;
        private String education = "";
        private String skills = "";
        private String experience = "Fresher";
        private String location = "";
        private double expectedSalary = 0;

        public CandidateBuilder(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public CandidateBuilder setId(int id) { this.id = id; return this; }
        public CandidateBuilder setEducation(String education) { this.education = education; return this; }
        public CandidateBuilder setSkills(String skills) { this.skills = skills; return this; }
        public CandidateBuilder setExperience(String experience) { this.experience = experience; return this; }
        public CandidateBuilder setLocation(String location) { this.location = location; return this; }
        public CandidateBuilder setExpectedSalary(double expectedSalary) { this.expectedSalary = expectedSalary; return this; }

        public Candidate build() {
            if (name == null || name.trim().isEmpty())
                throw new IllegalStateException("Candidate name is required");
            if (age <= 0)
                throw new IllegalStateException("Candidate age must be positive");
            return new Candidate(this);
        }
    }
}
