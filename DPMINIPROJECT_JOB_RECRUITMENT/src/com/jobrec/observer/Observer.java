package com.jobrec.observer;

// OBSERVER PATTERN — every subscriber must implement this.
public interface Observer {
    void update(String message);
}
