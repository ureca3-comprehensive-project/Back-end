package org.backend.billing.message.dto.response;

public record MessageDetailResponse(
        Long id,
        Long userId,
        String channel,
        String destination,
        Long templateId,
        String status,
        String scheduledAt, // null 가능
        String createdAt,
        String updatedAt
) {}
