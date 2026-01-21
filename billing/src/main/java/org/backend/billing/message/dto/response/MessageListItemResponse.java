package org.backend.billing.message.dto.response;

public record MessageListItemResponse(
        Long id,
        Long userId,
        String channel,
        String destination,
        String status,
        String createdAt
) {}
