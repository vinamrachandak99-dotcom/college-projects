package com.jobrec.abstractfactory;

import com.jobrec.model.Job;

// ABSTRACT FACTORY PATTERN
// While Factory Method creates ONE object (a Job), Abstract Factory creates
// a whole FAMILY of related objects that belong together for a job category:
// the Job itself, its EligibilityCriteria, and its NotificationTemplate.
// This guarantees the 3 pieces always match the same category (e.g. all "IT").
public interface RecruitmentAbstractFactory {
    Job createJob(int id, String title, String requiredSkills, String location, double salary);
    EligibilityCriteria createEligibilityCriteria();
    NotificationTemplate createNotificationTemplate();
}
