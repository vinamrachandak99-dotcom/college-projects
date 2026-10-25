package com.jobrec.service;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;
import com.jobrec.strategy.MatchingStrategy;
import com.jobrec.strategy.SkillHeavyStrategy;

// STRATEGY PATTERN — the Context.
// JobMatcher no longer hard-codes the matching formula; it delegates to
// whichever MatchingStrategy is currently plugged in, and the client can
// swap strategies at runtime with setStrategy().
public class JobMatcher {

    private MatchingStrategy strategy;

    public JobMatcher() {
        this(new SkillHeavyStrategy());   // default keeps the original behaviour
    }

    public JobMatcher(MatchingStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(MatchingStrategy strategy) {
        if (strategy == null) throw new IllegalArgumentException("Strategy cannot be null");
        this.strategy = strategy;
    }

    public MatchingStrategy getStrategy() { return strategy; }

    public double matchPercentage(Candidate candidate, Job job) {
        return strategy.calculateMatch(candidate, job);
    }
}
