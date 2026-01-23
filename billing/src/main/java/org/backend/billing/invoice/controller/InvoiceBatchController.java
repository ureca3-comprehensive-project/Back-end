package org.backend.billing.invoice.controller;

import org.backend.core.port.InvoiceBatchPort;
import org.backend.core.dto.BatchRunRequest;
import org.backend.core.dto.BatchRunResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing/invoices")
@RequiredArgsConstructor
public class InvoiceBatchController {
    private final InvoiceBatchPort invoiceBatchPort;

    // 배치 요약 조회
    @GetMapping("/{runId}/summary")
    public ResponseEntity<Map<String, Object>> getBatchSummary(@RequestParam String jobName) {
        return ResponseEntity.ok(invoiceBatchPort.getBatchSummary(jobName));
    }

    // 배치 실패 상세
    @GetMapping("/{runId}/errors")
    public ResponseEntity<List<String>> getBatchErrors(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(invoiceBatchPort.getJobErrors(runId));
    }

    // 배치 재처리
    @GetMapping("/{runId}/retry")
    public ResponseEntity<Long> retryBatch(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(invoiceBatchPort.retryJob(runId));
    }

    // 배치 취소
    @PostMapping("/{runId}/cancel")
    public ResponseEntity<Void> stopBatch(@PathVariable("runId") Long runId) {
        invoiceBatchPort.stopJob(runId);
        return ResponseEntity.ok().build();
    }

    // 배치 실행 목록 조회 (대시보드)
    @GetMapping
    public ResponseEntity<List<BatchRunResponse>> getBatchRuns(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(invoiceBatchPort.getJobExecutions(offset, limit));
    }

    // 배치 실행 단건 조회
    @GetMapping("/{runId}")
    public ResponseEntity<BatchRunResponse> getBatchRunDetail(@PathVariable("runId") Long runId) {
        return ResponseEntity.ok(invoiceBatchPort.getJobExecutionDetail(runId));
    }

    // 배치 (스케줄러)
    @PostMapping("/schedules")
    public ResponseEntity<Long> runBatch(@RequestBody BatchRunRequest request) {
        return ResponseEntity.ok(invoiceBatchPort.runJob(request));
    }

    // 배치 (수동 트리거) - test에서 빠짐
    @PostMapping
    public ResponseEntity<Long> runBatchManually(@RequestBody BatchRunRequest request) {
        Long executionId = invoiceBatchPort.runJobManually(request);
        if (executionId == null) {
            return ResponseEntity.status(409).build(); // 락 획득 실패 시
        }
        return ResponseEntity.ok(executionId);
    }

    // 청구서 중복 검증
    @GetMapping("/validations/duplicates")
    public ResponseEntity<Map<String, Object>> getDuplicateReport(@RequestParam String billingMonth) {
        return ResponseEntity.ok(invoiceBatchPort.getDuplicateReport(billingMonth));
    }
}
