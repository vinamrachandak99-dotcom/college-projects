package com.jobrec.bridge;

public class JobAlertNotification extends Notification {
    public JobAlertNotification(NotificationMethod method) {
        super(method);
    }

    @Override
    public void notify(String to, String content) {
        method.send(to, "[Job Alert] " + content);
    }
}
