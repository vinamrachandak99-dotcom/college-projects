package com.jobrec.abstractfactory;

// Another "family" product — the message wording used for this job category.
public interface NotificationTemplate {
    String buildMessage(String candidateName, String jobTitle);
}
