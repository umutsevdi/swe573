package com.hive.app.async.notification.model;

public enum NotificationType {
    EVENT_EDIT, // Event details has changed.
    EVENT_NEGOTIATING, // A user sent invitation.
    EVENT_ACCEPTED, // You were accepted to the event.
    EVENT_REJECTED, // You were rejected from the event.
    EVENT_CANCELLED, // Event has been canceled.
    EVENT_COMPLETED,// Event has been completed, ready for review.
    THREAD_REPLY, // Thread you previously participated has received a new reply.
    THREAD_CLOSED, // Thread you previously participated has been closed.
    USER_ROLE_UPDATE, // You were promoted/demoted
}
