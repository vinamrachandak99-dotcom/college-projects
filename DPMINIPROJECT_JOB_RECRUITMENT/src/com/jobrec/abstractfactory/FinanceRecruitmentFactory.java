package com.jobrec.abstractfactory;

import com.jobrec.factory.FinanceJob;
import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

public class FinanceRecruitmentFactory implements RecruitmentAbstractFactory {

    @Override
    public Job createJob(int id, String title, String requiredSkills, String location, double salary) {
        return new FinanceJob(id, title, requiredSkills, location, salary);
    }

    @Override
    public EligibilityCriteria createEligibilityCriteria() {
        // Finance roles: require at least a Bachelor's-level education string
        return (Candidate c) -> c.getEducation().toLowerCase().contains("b.") ||
                c.getEducation().toLowerCase().contains("m.");
    }

    @Override
    public NotificationTemplate createNotificationTemplate() {
        return (name, jobTitle) -> "Hi " + name + ", a new Finance opening '" + jobTitle +
                "' fits your profile!";
    }
}
