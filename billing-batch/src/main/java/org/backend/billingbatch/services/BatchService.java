package org.backend.billingbatch.services;

import lombok.extern.slf4j.Slf4j;
import org.backend.core.dto.BatchRunRequest;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BatchService {
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Job invoiceJob;

    public BatchService(JobLauncher jobLauncher,
                        JobExplorer jobExplorer,
                        JobOperator jobOperator,
                        @Qualifier("createInvoiceJob") Job invoiceJob) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobOperator = jobOperator;
        this.invoiceJob = invoiceJob;
    }

    // ==========================
    // 1. 실행 (Trigger)
    // ==========================
    public Long runJob(BatchRunRequest request) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("billingMonth", request.getBillingMonth())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            return jobLauncher.run(invoiceJob, params).getId();
        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            throw new RuntimeException("배치 실행 실패: " + request.getJobName(), e);
        }
    }

    // ==========================
    // 2. 제어 (Control)
    // ==========================
    public Long retryJob(Long executionId) {
        try {
            return jobOperator.restart(executionId);
        } catch (Exception e) {
            throw new RuntimeException("배치 재시작 실패", e);
        }
    }

    public void stopJob(Long executionId) {
        try {
            jobOperator.stop(executionId);
        } catch (Exception e) {
            throw new RuntimeException("배치 중지 실패", e);
        }
    }

    // ==========================
    // 3. 조회 (Query) - Entity 반환
    // ==========================

    // 단건 조회 (JobExecution 반환 -> Adapter에서 변환)
    public JobExecution getJobExecutionEntity(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new RuntimeException("Execution not found: " + executionId);
        }
        return execution;
    }

    // 목록 조회 (JobExecution 리스트 반환)
    public List<JobExecution> getJobExecutionsEntityList(int offset, int limit) {
        List<JobInstance> instances = jobExplorer.getJobInstances("createInvoiceJob", offset, limit);
        List<JobExecution> executions = new ArrayList<>();

        for (JobInstance instance : instances) {
            executions.addAll(jobExplorer.getJobExecutions(instance));
        }
        // ID 역순 정렬 (최신순)
        executions.sort((o1, o2) -> Long.compare(o2.getId(), o1.getId()));
        return executions;
    }

    public List<String> getJobErrors(Long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) return Collections.emptyList();

        return execution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getBatchSummary(String jobName) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("jobName", jobName);
        try {
            long instanceCount = jobExplorer.getJobInstanceCount(jobName);
            summary.put("totalInstanceCount", instanceCount);
        } catch (NoSuchJobException e) {
            summary.put("totalInstanceCount", 0);
            summary.put("status", "JOB_NOT_FOUND");
        }
        return summary;
    }

    public Map<String, Object> getDuplicateReport(String billingMonth) {
        return Map.of("status", "SAFE", "month", billingMonth);
    }
}
