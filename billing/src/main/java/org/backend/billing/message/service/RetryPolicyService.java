package org.backend.billing.message.service;

import java.util.Map;

import org.backend.billing.message.entity.RetryPolicyEntity;
import org.backend.billing.message.repository.RetryPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetryPolicyService {

    private final RetryPolicyRepository retryPolicyRepository;

    public RetryPolicyService(RetryPolicyRepository retryPolicyRepository) {
        this.retryPolicyRepository = retryPolicyRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get() {
        RetryPolicyEntity p = getOrCreate();
        return toMap(p);
    }

    @Transactional
    public Map<String, Object> update(org.backend.billing.message.dto.request.RetryPolicyUpdateRequest req) {
        RetryPolicyEntity p = getOrCreate();

        // req 필드가 primitive면 null 체크가 불가능하니 "그대로 덮어쓰기"가 맞음
        // (만약 부분 수정 PATCH를 하고 싶으면 DTO를 Integer/Long/Double로 바꿔야 함)
        p.patch(
                req.maxAttempts(),
                req.baseDelayMillis(),
                req.backoffMultiplier(),
                req.timeoutMillis(),
                req.emailFailRate()
        );

        return toMap(p);
    }

    private RetryPolicyEntity getOrCreate() {
        return retryPolicyRepository.findById(1L)
                .orElseGet(() -> retryPolicyRepository.save(new RetryPolicyEntity()));
    }

    private Map<String, Object> toMap(RetryPolicyEntity p) {
        return Map.of(
                "maxAttempts", p.getMaxAttempts(),
                "baseDelayMillis", p.getBaseDelayMillis(),
                "backoffMultiplier", p.getBackoffMultiplier(),
                "timeoutMillis", p.getTimeoutMillis(),
                "emailFailRate", p.getEmailFailRate(),
                "updatedAt", p.getUpdatedAt().toString()
        );
    }
}