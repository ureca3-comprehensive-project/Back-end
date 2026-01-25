package org.backend.core.dto;

import lombok.Builder;
import lombok.Getter;

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
    private Long writeCount; // 배치 작업 개수 현황
}
