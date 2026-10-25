package com.jobrec.strategy;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

// CONCRETE STRATEGY — skills matter most.
// Same weighting as the original JobMatcher, so default results are unchanged:
//   skills 60%  |  salary fit 25%  |  location 15%
public class SkillHeavyStrategy implements MatchingStrategy {

    @Override
    public double calculateMatch(Candidate c, Job j) {
        double score = MatchScores.skillScore(c, j) * 0.60
                     + MatchScores.salaryScore(c, j) * 0.25
                     + MatchScores.locationScore(c, j) * 0.15;
        return MatchScores.toPercent(score);
    }

    @Override
    public String getName() { return "Skill-Heavy"; }
}
