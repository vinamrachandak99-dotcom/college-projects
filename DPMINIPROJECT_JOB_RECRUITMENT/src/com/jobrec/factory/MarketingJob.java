package com.jobrec.factory;

import com.jobrec.model.Job;

public class MarketingJob extends Job {
    public MarketingJob(int id, String title, String requiredSkills, String location, double offeredSalary) {
        super(id, title, requiredSkills, location, offeredSalary, "Marketing");
    }

    @Override
    public String describe() {
        return "[Marketing Job] " + getTitle() + " needs skills: " + getRequiredSkills();
    }
}
