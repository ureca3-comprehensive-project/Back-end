package org.backend.billing.message.dto.response;

public record AttemptHistoryResponse(
        Long attemptId,
        Long messageId,
        int attemptNo,
        String status,
        String provider,
        int httpStatus,
        String createdAt
) {}
