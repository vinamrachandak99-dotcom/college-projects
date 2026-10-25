package com.jobrec.service;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Not a design pattern by itself — just the matching-percentage logic
// mentioned in the feature list (skills overlap + location match).
public class JobMatcher {

    public static double matchPercentage(Candidate candidate, Job job) {
        Set<String> candidateSkills = new HashSet<>(Arrays.asList(candidate.getSkills().toLowerCase().split("\\s*,\\s*")));
        Set<String> jobSkills = new HashSet<>(Arrays.asList(job.getRequiredSkills().toLowerCase().split("\\s*,\\s*")));

        long overlap = candidateSkills.stream().filter(jobSkills::contains).count();
        double skillScore = jobSkills.isEmpty() ? 0 : (double) overlap / jobSkills.size();

        double locationScore = candidate.getLocation().equalsIgnoreCase(job.getLocation()) ? 1.0 : 0.5;
        double salaryScore = candidate.getExpectedSalary() <= job.getOfferedSalary() * 1.2 ? 1.0 : 0.5;

        // weighted: skills matter most, then salary fit, then location
        double score = (skillScore * 0.6) + (salaryScore * 0.25) + (locationScore * 0.15);
        return Math.round(score * 1000) / 10.0;   // percentage, 1 decimal place
    }
}
