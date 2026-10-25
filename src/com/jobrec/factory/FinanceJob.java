package com.jobrec.factory;

import com.jobrec.model.Job;

public class FinanceJob extends Job {
    public FinanceJob(int id, String title, String requiredSkills, String location, double offeredSalary) {
        super(id, title, requiredSkills, location, offeredSalary, "Finance");
    }

    @Override
    public String describe() {
        return "[Finance Job] " + getTitle() + " needs skills: " + getRequiredSkills();
    }
}
