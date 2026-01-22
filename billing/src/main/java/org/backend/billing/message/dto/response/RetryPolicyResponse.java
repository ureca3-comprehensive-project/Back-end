package org.backend.billing.message.dto.response;

public record RetryPolicyResponse(
        int maxAttempts,
        long baseDelayMillis,
        double backoffMultiplier,
        long timeoutMillis,
        double emailFailRate,
        String updatedAt
) {}
