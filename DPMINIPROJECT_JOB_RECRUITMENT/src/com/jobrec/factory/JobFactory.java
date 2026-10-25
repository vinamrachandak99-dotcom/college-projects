package com.jobrec.factory;

import com.jobrec.model.Job;

// FACTORY METHOD PATTERN
// The caller just says "give me an IT job" or "give me a Finance job" —
// it doesn't need to know which class (ITJob, FinanceJob...) gets built.
public class JobFactory {

    public static Job createJob(int id, String category, String title, String requiredSkills,
                                 String location, double offeredSalary) {
        switch (category.toLowerCase()) {
            case "it":
                return new ITJob(id, title, requiredSkills, location, offeredSalary);
            case "finance":
                return new FinanceJob(id, title, requiredSkills, location, offeredSalary);
            case "marketing":
                return new MarketingJob(id, title, requiredSkills, location, offeredSalary);
            default:
                throw new IllegalArgumentException("Unknown job category: " + category);
        }
    }
}
