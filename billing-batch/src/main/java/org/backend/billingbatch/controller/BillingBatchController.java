package org.backend.billingbatch.controller;

import org.backend.billingbatch.dto.BatchRunRequest;
import org.backend.billingbatch.dto.BatchRunResponse;
import org.backend.billingbatch.services.BatchService;
import org.backend.billingbatch.services.LockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing/runs")
@RequiredArgsConstructor
public class BillingBatchController {
    private final BatchService batchService;
    private final LockService lockService;

    // 정산 배치 요약 조회
    @GetMapping("/{runId}/summary")
    public ResponseEntity<Map<String, Object>> getBatchSummary(@RequestParam String jobName) {
        return ResponseEntity.ok(batchService.getBatchSummary(jobName));
    }

    // 정산 배치 실패 상세
    @GetMapping("/{runId}/errors")
    public ResponseEntity<List<String>> getBatchErrors(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(batchService.getJobErrors(runId));
    }

    // 정산 배치 재처리
    @GetMapping("/{runId}/retry")
    public ResponseEntity<Long> retryBatch(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(batchService.retryJob(runId));
    }

    // 정산 배치 취소
    @PostMapping("/{runId}/cancel")
    public ResponseEntity<Void> stopBatch(@PathVariable("runId") Long runId) {
        batchService.stopJob(runId);
        return ResponseEntity.ok().build();
    }

    // 정산 배치 실행 목록 조회 (대시보드)
    @GetMapping
    public ResponseEntity<List<BatchRunResponse>> getBatchRuns(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(batchService.getJobExecutions(offset, limit));
    }

    // 정산 배치 실행 단건 조회
    @GetMapping("/{runId}")
    public ResponseEntity<BatchRunResponse> getBatchRunDetail(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(batchService.getJobExecutionDetail(runId));
    }

    // 정산 배치 (스케줄러)
    @PostMapping("/schedules")
    public ResponseEntity<Long> runBatch(@RequestBody BatchRunRequest request) {
        return ResponseEntity.ok(batchService.runJob(request));
    }

    // 정산 배치 (수동 트리거) - test에서 빠짐
    @PostMapping
    public ResponseEntity<Long> runBatchManually(@RequestBody BatchRunRequest request) {
        // 동시성 락 체크 후 실행 및 해제
        if (!lockService.tryAcquireLock()) {
            return ResponseEntity.status(409).build();
        }
        try {
            return ResponseEntity.ok(batchService.runJob(request));
        } finally {
            lockService.forceUnlock();
        }
    }

    // 정산 중복 검증
    @GetMapping("/validations/duplicates")
    public ResponseEntity<Map<String, Object>> getDuplicateReport(@RequestParam String billingMonth) {
        return ResponseEntity.ok(batchService.getDuplicateReport(billingMonth));
    }
}
