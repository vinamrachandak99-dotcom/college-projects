package com.jobrec.bridge;

public class StatusUpdateNotification extends Notification {
    public StatusUpdateNotification(NotificationMethod method) {
        super(method);
    }

    @Override
    public void notify(String to, String content) {
        method.send(to, "[Status Update] " + content);
    }
}
