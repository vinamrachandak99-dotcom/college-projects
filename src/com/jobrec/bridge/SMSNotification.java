package com.jobrec.bridge;

public class SMSNotification implements NotificationMethod {
    @Override
    public void send(String to, String message) {
        System.out.println("[SMS to " + to + "] " + message);
    }
}
