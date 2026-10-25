package com.jobrec.abstractfactory;

import com.jobrec.factory.MarketingJob;
import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

public class MarketingRecruitmentFactory implements RecruitmentAbstractFactory {

    @Override
    public Job createJob(int id, String title, String requiredSkills, String location, double salary) {
        return new MarketingJob(id, title, requiredSkills, location, salary);
    }

    @Override
    public EligibilityCriteria createEligibilityCriteria() {
        // Marketing roles: any candidate above 20 years old is considered eligible
        return (Candidate c) -> c.getAge() >= 20;
    }

    @Override
    public NotificationTemplate createNotificationTemplate() {
        return (name, jobTitle) -> "Hi " + name + ", check out this Marketing role '" + jobTitle + "'!";
    }
}
