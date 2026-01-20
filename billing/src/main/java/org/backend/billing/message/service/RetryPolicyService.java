package org.backend.billing.message.service;

import java.util.Map;
import org.backend.billing.message.dto.request.RetryPolicyUpdateRequest;
import org.springframework.stereotype.Service;

@Service
public class RetryPolicyService {

    private final InMemoryStores stores;

    public RetryPolicyService(InMemoryStores stores) {
        this.stores = stores;
    }

    public Map<String, Object> get() {
        var p = stores.retryPolicy;
        return Map.of(
                "maxAttempts", p.maxAttempts,
                "baseDelayMillis", p.baseDelayMillis,
                "backoffMultiplier", p.backoffMultiplier,
                "timeoutMillis", p.timeoutMillis,
                "emailFailRate", p.emailFailRate,
                "updatedAt", p.updatedAt.toString()
        );
    }

    public Map<String, Object> update(RetryPolicyUpdateRequest req) {
        var p = stores.retryPolicy;
        p.maxAttempts = req.maxAttempts();
        p.baseDelayMillis = req.baseDelayMillis();
        p.backoffMultiplier = req.backoffMultiplier();
        p.timeoutMillis = req.timeoutMillis();
        p.emailFailRate = req.emailFailRate();
        p.updatedAt = java.time.LocalDateTime.now();
        return get();
    }
}