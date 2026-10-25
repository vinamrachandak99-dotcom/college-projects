package com.jobrec.abstractfactory;

import com.jobrec.factory.ITJob;
import com.jobrec.model.Candidate;
import com.jobrec.model.Job;

public class ITRecruitmentFactory implements RecruitmentAbstractFactory {

    @Override
    public Job createJob(int id, String title, String requiredSkills, String location, double salary) {
        return new ITJob(id, title, requiredSkills, location, salary);
    }

    @Override
    public EligibilityCriteria createEligibilityCriteria() {
        // IT roles: candidate must mention at least one tech skill
        return (Candidate c) -> c.getSkills().toLowerCase().matches(".*(java|python|sql|c\\+\\+|javascript).*");
    }

    @Override
    public NotificationTemplate createNotificationTemplate() {
        return (name, jobTitle) -> "Hi " + name + ", a new IT opening '" + jobTitle +
                "' matches your technical profile!";
    }
}
