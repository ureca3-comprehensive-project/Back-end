package org.backend.billing.message.dto.request;

public record ResendRequest(
        Long messageId,
        String fallbackPhone
) {}