package com.jobrec.model;

import java.util.ArrayList;
import java.util.List;
import com.jobrec.observer.Observer;

// Application acts as the "request" traveling through the Chain of
// Responsibility, and as the "Subject" in the Observer pattern —
// it notifies subscribed candidates whenever its status changes.
public class Application {
    private int id;
    private Candidate candidate;
    private Job job;
    private String currentStage = "Submitted";
    private String status = "Pending";       // Pending, Shortlisted, Rejected, Hired
    private boolean rejected = false;

    private List<Observer> subscribers = new ArrayList<>();

    public Application(int id, Candidate candidate, Job job) {
        this.id = id;
        this.candidate = candidate;
        this.job = job;
    }

    public int getId() { return id; }
    public Candidate getCandidate() { return candidate; }
    public Job getJob() { return job; }
    public String getCurrentStage() { return currentStage; }
    public String getStatus() { return status; }
    public boolean isRejected() { return rejected; }

    public void setCurrentStage(String stage) { this.currentStage = stage; }

    public void subscribe(Observer o) {
        subscribers.add(o);
    }

    public void setStatus(String status) {
        this.status = status;
        notifySubscribers("Application #" + id + " for '" + job.getTitle() +
                "' status changed to: " + status);
    }

    public void markRejected(String atStage) {
        this.rejected = true;
        setStatus("Rejected at " + atStage);
    }

    private void notifySubscribers(String message) {
        for (Observer o : subscribers) {
            o.update(message);
        }
    }
}
