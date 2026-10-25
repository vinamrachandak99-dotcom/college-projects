package com.jobrec.bridge;

// BRIDGE PATTERN — "Implementor" side.
// This defines HOW a message actually gets sent, separate from WHAT kind
// of notification it is. New delivery channels (WhatsApp, Push...) can be
// added here without touching the Notification classes below.
public interface NotificationMethod {
    void send(String to, String message);
}
