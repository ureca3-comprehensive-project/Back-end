package org.backend.billingbatch.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.batch.core.job.JobExecution;

import java.time.LocalDateTime;

@Getter
@Builder
public class BatchRunResponse {
    private Long jobExecutionId;
    private String jobName;
    private String status;       // RUNNING 작동중, COMPLETED 완료, FAILED 실패, STOPPED 정지
    private String exitCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String billingMonth; // JobParameter에서 꺼낼 값

    // Spring Batch 객체 -> DTO로 변환하는 메서드
    public static BatchRunResponse from(JobExecution execution) {
        return BatchRunResponse.builder()
                .jobExecutionId(execution.getId())
                .jobName(execution.getJobInstance().getJobName())
                .status(execution.getStatus().toString())
                .exitCode(execution.getExitStatus().getExitCode())
                .startTime(execution.getStartTime())
                .endTime(execution.getEndTime())
                .billingMonth(execution.getJobParameters().getString("billingMonth"))
                .build();
    }
}
