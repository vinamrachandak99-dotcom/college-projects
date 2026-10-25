package com.jobrec.strategy;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

// CONCRETE STRATEGY — years of experience matter most.
//   experience 50%  |  skills 30%  |  salary fit 10%  |  location 10%
// Experience is scored linearly: Fresher = 0, 5+ years = full marks.
public class ExperienceHeavyStrategy implements MatchingStrategy {

    @Override
    public double calculateMatch(Candidate c, Job j) {
        double score = MatchScores.experienceScore(c) * 0.50
                     + MatchScores.skillScore(c, j) * 0.30
                     + MatchScores.salaryScore(c, j) * 0.10
                     + MatchScores.locationScore(c, j) * 0.10;
        return MatchScores.toPercent(score);
    }

    @Override
    public String getName() { return "Experience-Heavy"; }
}
