package org.backend.billing.message.controller;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.dto.request.RetryPolicyUpdateRequest;
import org.backend.billing.message.dto.response.RetryPolicyResponse;
import org.backend.billing.message.entity.RetryPolicyEntity;
import org.backend.billing.message.repository.RetryPolicyRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages/retry-policy")
public class RetryPolicyController {

    private final RetryPolicyRepository retryPolicyRepository;

    public RetryPolicyController(RetryPolicyRepository retryPolicyRepository) {
        this.retryPolicyRepository = retryPolicyRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ApiResponse<RetryPolicyResponse> getPolicy() {
        RetryPolicyEntity p = getOrCreate();
        return ApiResponse.ok(toResponse(p));
    }

    /**
     * ⚠️ 주의:
     * RetryPolicyUpdateRequest가 primitive 타입이라 null이 못 들어옴.
     * 그래서 PATCH라도 "전체 값을 덮어쓰기"로 동작함.
     */
    @PatchMapping
    @Transactional
    public ApiResponse<RetryPolicyResponse> updatePolicy(@RequestBody RetryPolicyUpdateRequest req) {
        RetryPolicyEntity p = getOrCreate();

        // primitive라 null 체크 불가 -> 전체 덮어쓰기(그래도 patch 메서드 재사용)
        p.patch(
                req.maxAttempts(),
                req.baseDelayMillis(),
                req.backoffMultiplier(),
                req.timeoutMillis(),
                req.emailFailRate()
        );

        return ApiResponse.ok(toResponse(p));
    }

    private RetryPolicyEntity getOrCreate() {
        return retryPolicyRepository.findById(1L)
                .orElseGet(() -> retryPolicyRepository.save(new RetryPolicyEntity()));
    }

    private RetryPolicyResponse toResponse(RetryPolicyEntity p) {
        return new RetryPolicyResponse(
                p.getMaxAttempts(),
                p.getBaseDelayMillis(),
                p.getBackoffMultiplier(),
                p.getTimeoutMillis(),
                p.getEmailFailRate(),
                p.getUpdatedAt().toString()
        );
    }
}