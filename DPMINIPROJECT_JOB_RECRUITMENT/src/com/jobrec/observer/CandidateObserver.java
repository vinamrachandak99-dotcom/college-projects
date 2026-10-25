package com.jobrec.observer;

import com.jobrec.bridge.Notification;
import com.jobrec.bridge.StatusUpdateNotification;
import com.jobrec.bridge.NotificationMethod;
import com.jobrec.model.Candidate;

// CONCRETE OBSERVER
// Wraps a Candidate and, when notified, sends the update out using the
// Bridge pattern's Notification + NotificationMethod (Email/SMS) — this is
// where Observer and Bridge connect in the project.
public class CandidateObserver implements Observer {

    private Candidate candidate;
    private String contactAddress;     // email or phone number
    private NotificationMethod deliveryMethod;

    public CandidateObserver(Candidate candidate, String contactAddress, NotificationMethod deliveryMethod) {
        this.candidate = candidate;
        this.contactAddress = contactAddress;
        this.deliveryMethod = deliveryMethod;
    }

    @Override
    public void update(String message) {
        Notification notification = new StatusUpdateNotification(deliveryMethod);
        notification.notify(contactAddress, message + " (for " + candidate.getName() + ")");
    }
}
