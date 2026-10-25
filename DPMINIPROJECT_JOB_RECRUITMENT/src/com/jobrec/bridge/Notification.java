package com.jobrec.bridge;

// BRIDGE PATTERN — "Abstraction" side.
// This defines WHAT kind of notification it is (status update, job alert).
// It holds a reference to a NotificationMethod (the "bridge") and delegates
// the actual sending to it, so the two hierarchies can vary independently.
public abstract class Notification {
    protected NotificationMethod method;   // the bridge to the delivery channel

    public Notification(NotificationMethod method) {
        this.method = method;
    }

    public abstract void notify(String to, String content);
}
