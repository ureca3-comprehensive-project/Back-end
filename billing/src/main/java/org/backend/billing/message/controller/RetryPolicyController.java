package org.backend.billing.message.controller;

import java.util.Map;

import org.backend.billing.common.ApiResponse;
import org.backend.billing.message.service.InMemoryStores;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages/retry-policy")
public class RetryPolicyController {

    private final InMemoryStores stores;

    public RetryPolicyController(InMemoryStores stores) {
        this.stores = stores;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> getPolicy() {
        var p = stores.retryPolicy;
        return ApiResponse.ok(Map.of(
                "maxAttempts", p.maxAttempts,
                "baseDelayMillis", p.baseDelayMillis,
                "backoffMultiplier", p.backoffMultiplier,
                "emailFailRate", p.emailFailRate
        ));
    }

    @PatchMapping
    public ApiResponse<Map<String, Object>> updatePolicy(@RequestBody RetryPolicyUpdateRequest req) {
        var p = stores.retryPolicy;

        if (req.maxAttempts() != null) p.maxAttempts = req.maxAttempts();
        if (req.baseDelayMillis() != null) p.baseDelayMillis = req.baseDelayMillis();
        if (req.backoffMultiplier() != null) p.backoffMultiplier = req.backoffMultiplier();
        if (req.emailFailRate() != null) p.emailFailRate = req.emailFailRate();

        return getPolicy();
    }

    // 컨트롤러 내부 record DTO (파일 추가 안 하고 이 파일에 같이 둠)
    public record RetryPolicyUpdateRequest(
            Integer maxAttempts,
            Long baseDelayMillis,
            Double backoffMultiplier,
            Double emailFailRate
    ) {}
}
