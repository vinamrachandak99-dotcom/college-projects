package com.jobrec.factory;

import com.jobrec.model.Job;

public class ITJob extends Job {
    public ITJob(int id, String title, String requiredSkills, String location, double offeredSalary) {
        super(id, title, requiredSkills, location, offeredSalary, "IT");
    }

    @Override
    public String describe() {
        return "[IT Job] " + getTitle() + " needs skills: " + getRequiredSkills();
    }
}
