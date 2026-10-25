package com.jobrec.strategy;

import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

// STRATEGY PATTERN — the Strategy interface.
// Each implementation is a different, interchangeable matching algorithm.
public interface MatchingStrategy {

    // Returns a match percentage in the range 0.0 – 100.0
    double calculateMatch(Candidate c, Job j);

    // Human-readable name shown in the UI
    String getName();
}
