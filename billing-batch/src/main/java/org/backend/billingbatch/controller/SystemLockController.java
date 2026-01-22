package org.backend.billingbatch.controller;

import lombok.RequiredArgsConstructor;
import org.backend.billingbatch.dto.LockStatusResponse;
import org.backend.billingbatch.services.LockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 단일 서버 기준
@RestController
@RequestMapping("/billing/locks")
@RequiredArgsConstructor
public class SystemLockController {
    private final LockService lockService;

    // 락 강제 해제
    @DeleteMapping
    public ResponseEntity<Void> unlock() {
        lockService.forceUnlock();
        return ResponseEntity.ok().build();
    }

    // 락 상태 조회
    @GetMapping
    public ResponseEntity<LockStatusResponse> getLockStatus() {
        return ResponseEntity.ok(lockService.getLockStatus());
    }

    // 동시성 테스트를 위해 추가
    @PostMapping("/acquire")
    public ResponseEntity<String> acquireLock() {
        boolean acquired = lockService.tryAcquireLock();

        if (acquired) {
            return ResponseEntity.ok("SUCCESS"); // 1명만 받음
        } else {
            return ResponseEntity.status(409).body("FAIL"); // 나머지는 실패
        }
    }
}
