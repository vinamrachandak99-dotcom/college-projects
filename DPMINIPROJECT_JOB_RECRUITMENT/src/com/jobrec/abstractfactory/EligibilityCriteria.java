package com.jobrec.abstractfactory;

import com.jobrec.model.Candidate;

// One of the "family" products created by the Abstract Factory.
public interface EligibilityCriteria {
    // returns true if candidate meets this category's minimum bar
    boolean isEligible(Candidate candidate);
}
