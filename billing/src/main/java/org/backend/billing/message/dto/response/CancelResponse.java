package org.backend.billing.message.dto.response;

public record CancelResponse(
        Long messageId,
        String status
) {}