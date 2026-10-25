package com.jobrec.bridge;

public class EmailNotification implements NotificationMethod {
    @Override
    public void send(String to, String message) {
        System.out.println("[EMAIL to " + to + "] " + message);
    }
}
