package org.backend.billing.message.dto.response;

public record MessageCreateResponse(
        Long messageId,
        String status
) {}
