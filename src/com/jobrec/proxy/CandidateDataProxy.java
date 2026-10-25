package com.jobrec.proxy;

import com.jobrec.model.Candidate;

// PROXY PATTERN
// Sits in front of the real data-access object and checks permission
// BEFORE letting the request through. The caller uses this proxy exactly
// like the real thing — it doesn't know (or need to know) it's a proxy.
public class CandidateDataProxy implements CandidateDataAccess {

    private RealCandidateDataAccess realAccess;
    private String requesterRole;   // e.g. "RECRUITER", "CANDIDATE", "GUEST"

    public CandidateDataProxy(String requesterRole) {
        this.requesterRole = requesterRole;
        this.realAccess = new RealCandidateDataAccess();
    }

    @Override
    public Candidate getCandidateDetails(int candidateId) {
        if (!requesterRole.equals("RECRUITER") && !requesterRole.equals("CANDIDATE")) {
            System.out.println("[Proxy] Access denied for role: " + requesterRole);
            return null;
        }
        System.out.println("[Proxy] Access granted for role: " + requesterRole + ". Fetching record...");
        return realAccess.getCandidateDetails(candidateId);
    }
}
