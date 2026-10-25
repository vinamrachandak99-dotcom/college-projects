package com.jobrec.proxy;

import com.jobrec.model.Candidate;

// Common interface for the real object and its proxy.
public interface CandidateDataAccess {
    Candidate getCandidateDetails(int candidateId);
}
