package org.backend.billing.invoice.controller;

import lombok.RequiredArgsConstructor;
import org.backend.core.dto.LockStatusResponse;
import org.backend.core.port.InvoiceBatchPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 단일 서버 기준
@RestController
@RequestMapping("/billing/locks")
@RequiredArgsConstructor
public class SystemLockController {
    private final InvoiceBatchPort invoiceBatchPort;

    // 락 강제 해제
    @DeleteMapping
    public ResponseEntity<Void> unlock() {
        invoiceBatchPort.forceUnlock();
        return ResponseEntity.ok().build();
    }

    // 락 상태 조회
    @GetMapping
    public ResponseEntity<LockStatusResponse> getLockStatus() {
        return ResponseEntity.ok(invoiceBatchPort.getLockStatus());
    }

    // 동시성 테스트를 위해 추가
//    @PostMapping("/acquire")
//    public ResponseEntity<String> acquireLock() {
//        boolean acquired = invoiceBatchPort.tryAcquireLock();
//
//        if (acquired) {
//            return ResponseEntity.ok("SUCCESS"); // 1명만 받음
//        } else {
//            return ResponseEntity.status(409).body("FAIL"); // 나머지는 실패
//        }
//    }
}
