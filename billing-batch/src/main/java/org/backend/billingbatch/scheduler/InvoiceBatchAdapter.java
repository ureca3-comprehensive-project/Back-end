package org.backend.billingbatch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.core.dto.BatchRunRequest;
import org.backend.core.dto.BatchRunResponse;
import org.backend.billingbatch.services.BatchService;
import org.backend.billingbatch.services.LockService;
import org.backend.core.port.InvoiceBatchPort;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.backend.core.dto.LockStatusResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceBatchAdapter implements InvoiceBatchPort {

    private final BatchService batchService;
    private final LockService lockService;
    private final JdbcTemplate jdbcTemplate; // 복잡한 통계 쿼리용

    // --- [핵심] Entity -> DTO 변환 메서드 ---
    private BatchRunResponse toDto(JobExecution execution) {
        if (execution == null) return null;
        long totalWriteCount = execution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();
        return BatchRunResponse.builder()
                .jobExecutionId(execution.getId())
                .jobName(execution.getJobInstance().getJobName())
                .status(execution.getStatus().toString())
                .exitCode(execution.getExitStatus().getExitCode())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .billingMonth(execution.getJobParameters().getString("billingMonth"))
                .writeCount(totalWriteCount)
                .build();
    }

    // --- 조회 구현 ---
    @Override
    public List<BatchRunResponse> getJobExecutions(int offset, int limit) {
        // Service에서 Entity 리스트를 받아와서 여기서 DTO로 변환
        return batchService.getJobExecutionsEntityList(offset, limit).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BatchRunResponse getJobExecutionDetail(Long runId) {
        // Service에서 Entity를 받아와서 여기서 DTO로 변환
        return toDto(batchService.getJobExecutionEntity(runId));
    }

    @Override
    public Map<String, Object> getBatchSummary(String jobName) {
        return batchService.getBatchSummary(jobName);
    }

    @Override
    public List<String> getJobErrors(Long runId) {
        return batchService.getJobErrors(runId);
    }

    @Override
    public Map<String, Object> getDuplicateReport(String billingMonth) {
        return batchService.getDuplicateReport(billingMonth);
    }

    // --- 제어 구현 ---
    @Override
    public Long retryJob(Long runId) {
        return batchService.retryJob(runId);
    }

    @Override
    public void stopJob(Long runId) {
        batchService.stopJob(runId);
    }

    // --- 실행 구현 ---
    @Override
    public Long runJob(BatchRunRequest request) {
        return batchService.runJob(request);
    }

    @Override
    public Long runJobManually(BatchRunRequest request) {
        // 락 로직 적용
        if (!lockService.tryAcquireLock()) {
            log.warn("배치 실행 중복 방지: 락 획득 실패");
            return null;
        }
        try {
            return batchService.runJob(request);
        } finally {
            lockService.forceUnlock();
        }
    }

//    @Override
//    public Map<String, Long> getErrorSummary(Long runId) {
//        String sql = "SELECT EXIT_MESSAGE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID = ?";
//        List<String> messages = jdbcTemplate.queryForList(sql, String.class, runId);
//
//        // 예시: 간단한 키워드 기반 분류
//        long validationErrors = messages.stream().filter(m -> m.contains("Validation")).count();
//        long timeoutErrors = messages.stream().filter(m -> m.contains("Timeout")).count();
//
//        return Map.of("VALIDATION_ERROR", validationErrors, "SYSTEM_TIMEOUT", timeoutErrors);
//    }
//
//    @Override
//    public List<Map<String, Object>> getExecutionTrends() {
//        // 최근 일주일간의 성공/실패 건수를 가져오는 SQL
//        String sql = "SELECT DATE(create_time) as date, status, COUNT(*) as count " +
//                "FROM BATCH_JOB_EXECUTION " +
//                "GROUP BY DATE(create_time), status " +
//                "ORDER BY date DESC LIMIT 7";
//        return jdbcTemplate.queryForList(sql);
//    }

    // 락 상태 조회
    @Override
    public LockStatusResponse getLockStatus() {
        return lockService.getLockStatus();
    }

    // 락 강제 해제
    @Override
    public void forceUnlock() {
        lockService.forceUnlock();
    }
}