package org.backend.billing.message.dto.request;

public record RetryPolicyUpdateRequest(
        int maxAttempts,
        long baseDelayMillis,
        double backoffMultiplier,
        long timeoutMillis,
        double emailFailRate
) {}