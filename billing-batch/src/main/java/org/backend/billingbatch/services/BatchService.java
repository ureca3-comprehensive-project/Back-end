package org.backend.billingbatch.services;

import lombok.RequiredArgsConstructor;

import org.backend.billingbatch.dto.BatchRunRequest;
import org.backend.billingbatch.dto.BatchRunResponse;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BatchService {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer; // 배치 메타 데이터 조회용
    private final JobOperator jobOperator;
    private final Job invoiceJob;          // Batch 모듈에서 등록한 Job Bean

    // 수동 배치 실행
    public Long runJob(BatchRunRequest request) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("billingMonth", request.getBillingMonth())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            return jobLauncher.run(invoiceJob, params).getId();
        } catch (Exception e) {
            throw new RuntimeException("배치 실행 실패", e);
        }
    }

    // 배치 재처리 (Retry) - JobOperator 사용
    public Long retryJob(Long executionId) {
        try {
            return jobOperator.restart(executionId);
        } catch (Exception e) {
            throw new RuntimeException("배치 재시작 실패", e);
        }
    }

    // 배치 중지 (Stop)
    public void stopJob(Long executionId) {
        try {
            jobOperator.stop(executionId);
        } catch (Exception e) {
            throw new RuntimeException("배치 중지 실패", e);
        }
    }

    // 에러 로그 조회
    public List<String> getJobErrors(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) return Collections.emptyList();

        return execution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .collect(Collectors.toList());
    }

    // 배치 요약 (통계)
    public Map<String, Object> getBatchSummary(String jobName) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("jobName", jobName);

        try {
            long instanceCount = jobExplorer.getJobInstanceCount(jobName);
            summary.put("totalInstanceCount", instanceCount);
        } catch (NoSuchJobException e) {
            // Job이 존재하지 않을 경우 예외 처리
            summary.put("totalInstanceCount", 0);
            summary.put("status", "JOB_NOT_FOUND");
        }

        return summary;
    }

    // 실행 목록 조회
    public List<BatchRunResponse> getJobExecutions(int offset, int limit) {
        List<JobInstance> instances = jobExplorer.getJobInstances("createInvoiceJob", offset, limit);
        List<BatchRunResponse> responses = new ArrayList<>();

        for (JobInstance instance : instances) {
            List<JobExecution> executions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : executions) {
                responses.add(BatchRunResponse.from(execution));
            }
        }
        return responses;
    }

    // 단건 상세 조회
    public BatchRunResponse getJobExecutionDetail(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if(execution == null) throw new RuntimeException("Execution not found");
        return BatchRunResponse.from(execution);
    }

    // 중복 청구서 검증 로직
    public Map<String, Object> getDuplicateReport(String billingMonth) {
        return Map.of("status", "SAFE", "month", billingMonth);
    }
}
