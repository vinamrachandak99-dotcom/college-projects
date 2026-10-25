package com.jobrec.strategy;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Shared scoring helpers used by the concrete strategies.
// Each helper returns a score between 0.0 and 1.0.
final class MatchScores {
    private MatchScores() {}

    static double skillScore(Candidate c, Job j) {
        Set<String> candidateSkills = toSet(c.getSkills());
        Set<String> jobSkills = toSet(j.getRequiredSkills());
        if (jobSkills.isEmpty()) return 0;
        long overlap = candidateSkills.stream().filter(jobSkills::contains).count();
        return (double) overlap / jobSkills.size();
    }

    static double locationScore(Candidate c, Job j) {
        return c.getLocation().equalsIgnoreCase(j.getLocation()) ? 1.0 : 0.5;
    }

    static double salaryScore(Candidate c, Job j) {
        return c.getExpectedSalary() <= j.getOfferedSalary() * 1.2 ? 1.0 : 0.5;
    }

    // 0 years -> 0.0, 5+ years -> 1.0 (linear in between)
    static double experienceScore(Candidate c) {
        return Math.min(c.getExperienceYears() / 5.0, 1.0);
    }

    // score in 0..1 -> percentage with one decimal place
    static double toPercent(double score) {
        return Math.round(score * 1000) / 10.0;
    }

    private static Set<String> toSet(String csv) {
        Set<String> set = new HashSet<>();
        if (csv == null || csv.trim().isEmpty()) return set;
        set.addAll(Arrays.asList(csv.toLowerCase().trim().split("\\s*,\\s*")));
        return set;
    }
}
