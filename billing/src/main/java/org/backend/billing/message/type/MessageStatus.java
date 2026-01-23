package org.backend.billing.message.type;

public enum MessageStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    DUPLICATE,
    SKIPPED,
    DND_HOLD,
    CANCELED
}